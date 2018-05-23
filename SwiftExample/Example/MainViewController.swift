import UIKit
import AffiniPaySDK
import AffiniPaySDK.AFPChargeParams
import AffiniPaySDK.AFPChargeResult
import AffiniPaySDK.AFPNavigationController

class MainViewController: UIViewController {
    // swiftlint:disable line_length

    private var first: Bool = true
    private var transactionNav: UINavigationController?
    private var accountsViewController: AccountsViewController?
    private var afpAmountVC: AFPAmountVC?
    private var afpCustomerInfoVC: AFPCustomerInfoVC?
    private var afpCardEntryVC: AFPCardEntryVC?
    private var afpSignatureVC: AFPSignatureVC?
    private var account: [String: Any]?
    private var amount: String?

    // MARK: - Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
    }

    func setOrientation() {
        if isIphone() {
            UIDevice.current.setValue(UIInterfaceOrientation.portrait.rawValue, forKey: "orientation")
        }
    }

    override func viewDidAppear(_ animated: Bool) {
        print("AffiniPay SDK version: \(AffiniPaySDK.version())")
        guard self.first == true else { return }
        self.first = false
        self.showAccounts(accounts: nil)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Show/dismiss view controllers
    func showAccounts(accounts: [String: Any]?) {
        let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
        let viewController = storyboard.instantiateViewController(withIdentifier: "AccountsViewController")
        guard let accountsViewController = viewController as? AccountsViewController else { return }
        self.accountsViewController = accountsViewController
        let nav = UINavigationController(rootViewController: viewController)
        self.present(nav, animated: true, completion: nil)

        accountsViewController.accounts = accounts
        accountsViewController.successBlock = { [weak self](account) in
            self?.account = account
            guard let accountId = self?.account?["id"] as? String else { assert(false, "Account Id not found"); return }
            guard let publicKey = self?.account?["public_key"] as? String else { assert(false, "Public Key not found"); return }
            print("Selected account with id: \(accountId), public key: \(publicKey)")
            self?.transactionNav = AFPNavigationController(rootViewController: UIViewController())
            let amountVC = self?.showAmountSelection(fromVC: accountsViewController)
            self?.transactionNav?.setViewControllers([amountVC!], animated: false)
        }

        accountsViewController.errorBlock = { (error) in
            print(error)
        }
    }

    func showAmountSelection(fromVC: UIViewController) -> UIViewController {
        // Instantiate amount selection view controller
        self.afpAmountVC = AffiniPaySDK.getAmountVC({ [weak self](amountResult) in
            // Store amount
            self?.amount =  amountResult?.amount

            // Show customer Information entry
            _ = self?.showCustomerInfo(fromVC: fromVC)
        })

        // Add cancel button
        self.afpAmountVC?.navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: .cancel, target: nil, action: #selector(dismissAmountSelection))

        // Present amount selection view controller
        fromVC.present(transactionNav!, animated: true, completion: nil)

        return self.afpAmountVC!
    }

    func showCustomerInfo(fromVC: UIViewController) -> UIViewController {
        // Create input
        let afpCustomerInput = getCustomerInfoInput()

        // Instantiate customer information view controller
        self.afpCustomerInfoVC = AffiniPaySDK.getCustomerInfoVC(afpCustomerInput!, next: { [weak self](result) in
            // Show card entry
            self?.showCardEntry(fromVC: fromVC, customerInfoResult: result)
        })
        self.afpCustomerInfoVC?.title = "Customer Info"

        // Add cancel button
        self.afpCustomerInfoVC?.navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: .cancel, target: nil, action: #selector(dismissCustomerInfo))

        // Push amount selection view controller
        self.transactionNav?.pushViewController(self.afpCustomerInfoVC!, animated: true)

        return self.afpCustomerInfoVC!
    }

    func showCardEntry(fromVC: UIViewController, customerInfoResult: AFPCustomerInfoResult?) {
        // Create input
        let afpChargeInput = AFPChargeParams(publicKey: getPublicKey(), amount: getAmount(), customerInfo: customerInfoResult?.customerInfo)

        // Instantiate card entry view controller
        self.afpCardEntryVC = AffiniPaySDK.getCardEntryVC(afpChargeInput!, nextWithCompletionBlock: { [weak self](chargeResult, completionCallback) in
            // Check card entry return values
            guard let amountString = chargeResult?.amount else { assert(false, "Invalid amount"); return }
            guard let tokenId = chargeResult?.oneTimeToken else { assert(false, "Invalid charge token"); return }
            print("Amount: \(amountString)")
            print("One time token: \(tokenId)")

            if let paymentDataSource = chargeResult?.paymentDataSource {
                print("Payment data source: \(paymentDataSource)")
            }
            if let posFields = chargeResult?.posFields {
                print("Pos: \(posFields)")
            }

            // Create a charge on the backend using a one token, account id, and amnout
            let net: NetworkController = NetworkControllerImpl()
            net.createCharge(tokenId, self?.getAccountId(), Int(amountString)!, { [weak self](result, error) in
                // Show error message on card entry controller
                if error != nil {
                    self?.showAlertPopup(title: "AffiniPaySDK_Example: Charge Failed", message: (error?.localizedDescription)!, from: (self?.afpCardEntryVC)!, handler: { (_ alertAction) in
                    })

                    completionCallback?(false)
                    return
                }

                if let chargeId = net.getChargeIdFromChargeHttpResponse(result: result) {
                    if (net.getAccountIdFromChargeHttpResponse(result: result)) != nil {
                        if let amount = net.getAmountFromChargeHttpResponse(result: result) {
                            _ = self?.showSignature(fromVC: fromVC, chargeId: chargeId, amount: String(amount), merchantName: "Display name for the merchant who is receiving this payment, and it will wrap even if it is a long name.")
                            return
                        }
                    }
                }

                // Callback to signal card entry controller to finish
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    // Show customer Information entry
                    completionCallback?(true)
                }

                if let gwErrorMessage = net.getAllErrorMessagesFromChargeHttpResponse(result: result) {
                    self?.showAlertPopup(title: "AffiniPaySDK_Example: Charge Failed", message: gwErrorMessage, from: (self?.afpCardEntryVC)!, handler: { (_ alertAction) in
                        completionCallback?(false)
                    })
                } else {
                    self?.showAlertPopup(title: "AffiniPaySDK_Example: Charge Failed", message: "Unknown reason", from: (self?.afpCardEntryVC)!, handler: { (_ alertAction) in
                        completionCallback?(false)
                    })
                }
                print("result \(String(describing: result))")
                print("error \(String(describing: error))")
            })
        })
        self.afpCardEntryVC?.title = "Charge"

        // Add cancel button
        self.afpCardEntryVC?.navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: .cancel, target: nil, action: #selector(dismissCardEntry))

        // Push card entry view controller
        self.transactionNav?.pushViewController(self.afpCardEntryVC!, animated: true)
    }

    func showSignature(fromVC: UIViewController, chargeId: String?, amount: String?, merchantName: String?) -> UIViewController {
        // Create input
        let afpSignatureInput = AFPSignatureParams(transactionId: chargeId, amount: amount, merchant: merchantName)

        // Instantiate signature view controller
        self.afpSignatureVC = AffiniPaySDK.getSignatureVC(afpSignatureInput!, nextWithCompletionBlock: { [weak self](signatureResult, completionCallback) in
            print("Signature received: \(String(describing: signatureResult?.signatureString))")

            // Create a charge on the backend using a one token, account id, and amnout
            let net: NetworkController = NetworkControllerImpl()
            net.signCharge(chargeId, self?.getAccountId(), signatureResult?.signatureString, { [weak self] (result, error) in
                if error != nil {
                    self?.showAlertPopup(title: "Sign Error", message: "Could not sign charge: \(String(describing: error?.localizedDescription))", from: (self?.afpSignatureVC)!)
                    completionCallback?(false)
                    return
                }

                print("result \(String(describing: result))")
                print("error \(String(describing: error))")
                // Use charge controller result and dismiss controller
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.01) {
                    let parsedDict = net.getChargeResponse(result: result)
                    if let successMessage = parsedDict["success"] as? String {
                        self?.afpCardEntryVC?.dismiss(animated: true, completion: {
                            self?.setOrientation()
                            completionCallback?(true)
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak self] in
                                self?.showAlertPopup(title: "Charge Receipt", message: successMessage, from: (self?.accountsViewController)!)
                           }
                        })
                    } else if let errorMessage = parsedDict["error"] as? String {
                        completionCallback?(false)
                        self?.showAlertPopup(title: "Signature Failed :(", message: errorMessage, from: (self?.accountsViewController)!)
                    }
                }
            })

        })
        self.afpSignatureVC?.title = "Sign"

        // Push signature view controller
        self.transactionNav?.pushViewController(self.afpSignatureVC!, animated: true)
        self.afpSignatureVC!.navigationItem.setHidesBackButton(true, animated: true)

        return self.afpSignatureVC!
    }

    @objc func dismissAmountSelection(sender: UIBarButtonItem) {
        self.dismissTransaction(sender: sender, from: self.afpAmountVC!)
    }

    @objc func dismissCustomerInfo(sender: UIBarButtonItem) {
        self.dismissTransaction(sender: sender, from: self.afpCustomerInfoVC!)
    }

    @objc func dismissCardEntry(sender: UIBarButtonItem) {
        self.dismissTransaction(sender: sender, from: self.afpCardEntryVC!)
    }

    @objc func dismissSignature(sender: UIBarButtonItem) {
        self.dismissTransaction(sender: sender, from: self.afpSignatureVC!, message: "Are you sure you want discard the signature and go back to Accounts?")
    }

    @objc func dismissTransaction(sender: UIBarButtonItem, from: UIViewController, message: String? = nil) {
        DispatchQueue.main.async {
            let alertSuccess = UIAlertController(title: "Discard",
                                                 message: (message != nil) ? message: "Are you sure you want discard this payment and go back to Accounts?",
                                                 preferredStyle: UIAlertControllerStyle.alert)
            let okAction = UIAlertAction (title: "Discard",
                                          style: UIAlertActionStyle.default,
                                          handler: { [weak self] (_ alertAction) in
                                            self?.transactionNav?.dismiss(animated: true, completion: {
                                                self?.setOrientation()
                                            })
                                        })
            let cancelAction = UIAlertAction (title: "No", style: UIAlertActionStyle.cancel, handler: nil)
            alertSuccess.addAction(okAction)
            alertSuccess.addAction(cancelAction)
            from.present(alertSuccess, animated: true, completion: nil)
        }
    }
    // MARK: - Utilities
    func isIphone() -> Bool {
        return UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiom.phone
    }

    func showAlertPopup(title: String, message: String, from: UIViewController, handler: ((UIAlertAction) -> Swift.Void)? = nil) {
        DispatchQueue.main.async {
            let alertSuccess = UIAlertController(title: title, message: message, preferredStyle: UIAlertControllerStyle.alert)
            let okAction = UIAlertAction (title: "OK", style: UIAlertActionStyle.cancel, handler: handler)
            alertSuccess.addAction(okAction)
            from.present(alertSuccess, animated: true, completion: nil)
        }
    }

    func getAccountId() -> String {
        guard let accountId = self.account?["id"] as? String else { assert(false, "Account Id not found"); return "" }
        return accountId
    }

    func getPublicKey() -> String {
        guard let publicKey = self.account?["public_key"] as? String else { assert(false, "Public Key not found"); return "" }
        return publicKey
    }

    func getAmountInteger() -> NSInteger {
        guard let amount = self.amount else { assert(false, "Amount not set"); return 0 }
        return Int(amount)!
    }

    func getAmount() -> String {
        guard let amount = self.amount else { assert(false, "Amount not set"); return "0" }
        guard amount.count > 0 else { assert(false, "Amount not set"); return "0" }
        return amount
    }

    func getCustomerInfoInput() -> AFPCustomerInfoParams? {
        let customerInfoInput = AFPCustomerInfoParams(mandatoryFields: [AFPCustomerInfoType.name.rawValue, AFPCustomerInfoType.address1.rawValue, AFPCustomerInfoType.postalcode.rawValue])
        return customerInfoInput
    }

}
