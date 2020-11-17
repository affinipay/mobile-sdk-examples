import UIKit
import AffiniPaySDK

public class MainViewController: UIViewController, ClientDetailsControllerDelegate {
    // swiftlint:disable line_length

    private var first: Bool = true
    private var transactionNav: UINavigationController?
    var accountsViewController: AccountsViewController?
    var clientVC: ClientDetailsController?
    private var afpCardEntryVC: AFPCardEntryVC?
    private var account: [String: Any]?
    private var amount: String?

    public override func viewDidLoad() {
        super.viewDidLoad()
    }

    func setOrientation() {
        if isIphone() {
            UIDevice.current.setValue(UIInterfaceOrientation.portrait.rawValue, forKey: "orientation")
        }
    }
    func isIphone() -> Bool {
        return UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiom.phone
    }

    public override func viewDidAppear(_ animated: Bool) {
        print("AffiniPay SDK version: \(AffiniPaySDK.version())")
        guard self.first == true else { return }
        self.first = false
        self.showAccounts(accounts: nil)
    }

    public override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func showAccounts(accounts: [String: Any]?) {
        let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
        let accountsVC = storyboard.instantiateViewController(withIdentifier: "AccountsViewController")
        guard let accountsViewController = accountsVC as? AccountsViewController else { return }
        self.accountsViewController = accountsViewController
        let nav = UINavigationController(rootViewController: accountsViewController)
        nav.modalPresentationStyle = .fullScreen
        self.present(nav, animated: true, completion: nil)

        accountsViewController.accounts = accounts
        accountsViewController.successBlock = { [weak self](account) in
            self?.account = account
            guard let accountId = self?.account?["id"] as? String else { assert(false, "Account Id not found"); return }
            guard let publicKey = self?.account?["public_key"] as? String else { assert(false, "Public Key not found"); return }
            print("Selected account with id: \(accountId), public key: \(publicKey)")
            self?.transactionNav = AFPNavigationController(rootViewController: UIViewController())
            self?.transactionNav!.modalPresentationStyle = .fullScreen
            let cardVC = self?.showCardEntry(fromVC: accountsViewController)
            self?.transactionNav?.setViewControllers([cardVC!], animated: false)
            self?.transactionNav?.navigationBar.barTintColor = UIColor(red: 0/255.0, green: 53/255.0, blue: 102/255.0, alpha: 1)
            self?.transactionNav?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor.white]
        }

        accountsViewController.errorBlock = { (error) in
            print(error)
        }
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
    
    func getAccountName() -> String {
        guard let accountName = self.account?["name"] as? String else { assert(false, "Account name not found"); return "" }
        return accountName
    }

    func getAmountInteger() -> NSInteger {
        guard let amount = self.amount else { assert(false, "Amount not set"); return 0 }
        return Int(amount)!
    }
    
    func isTrustAccount() -> Bool {
        guard let isTrust = self.account?["trust_account"] as? Bool else { assert(false, "Account type not found"); return false }
        return isTrust
    }

    func getAmount() -> String {
        guard let amount = self.amount else { assert(false, "Amount not set"); return "0" }
        guard amount.count > 0 else { assert(false, "Amount not set"); return "0" }
        return amount
    }

    func integerFromString(_ string: String?) -> NSInteger {
        guard let amount = string else { return 0 }
        return Int(amount)!
    }
    // swiftlint:disable:next function_body_length
    func showCardEntry(fromVC: UIViewController) -> UIViewController {
        // Create input
        let afpTokenizationParams = AFPTokenizationInitParams(publicKey: getPublicKey(),
                                amount: "100",
                                accountName: getAccountName(),
                                trustAccount: isTrustAccount(),
                                requireCvv: true,
                                requireSwipeCvv: true,
                                disableCardReader: false,
                                autoShowHideNavigationBar: false)
        // Instantiate card entry view controller
        self.afpCardEntryVC = AffiniPaySDK.getCardEntryVC(afpTokenizationParams, onReturnSwiperData: { (swiperData) in
            // swiperData
            print("swiperData: \(swiperData)")
            
        }, onReturnCardData: { (data) in
            // data
            print("data: \(data)")
            // show client details page
            let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
            // swiftlint:disable:next force_cast superfluous_disable_command
            self.clientVC = storyboard.instantiateViewController(withIdentifier: "ClientDetailsController") as? ClientDetailsController
            self.clientVC!.delegate = self
            self.afpCardEntryVC?.navigationController?.pushViewController(self.clientVC!, animated: true)
        }, onReturnPaymentToken: { (result) in
            self.clientVC?.stopLoading()
            // callback on reset
            if (result!.error) != nil {
                // error
                self.showAlertPopup(title: "Error", message: (result?.error?.localizedDescription)!, from: (self.afpCardEntryVC)!, handler: { (_ alertAction) in
                    
                })
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    self.afpCardEntryVC?.finish(false)
                }
            } else {
                // Check card entry return values
                guard let amountString = result?.amount else { assert(false, "Invalid amount"); return }
                guard let tokenId = result?.oneTimeToken else { assert(false, "Invalid charge token"); return }
                print("Amount: \(amountString)")
                print("One time token: \(tokenId)")

                if let paymentDataSource = result?.paymentDataSource {
                    print("Payment data source: \(paymentDataSource)")
                }
                if let posFields = result?.posFields {
                    print("Pos: \(posFields)")
                }

                // Create a charge on the backend using a one token, account id, and amount
                let net: NetworkController = NetworkControllerImpl()
                net.createCharge(tokenId, self.getAccountId(), Int(amountString)!, { [weak self](result, error) in
                    // Show error message on card entry controller
                    if error != nil {
                        self?.showAlertPopup(title: "Error", message: (error?.localizedDescription)!, from: (self?.afpCardEntryVC)!, handler: { (_ alertAction) in
                            //
                        })
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            self?.afpCardEntryVC?.finish(false)
                        }
                        return
                    }

                    if let chargeId = net.getChargeIdFromChargeHttpResponse(result: result) {
                        if (net.getAccountIdFromChargeHttpResponse(result: result)) != nil {
                            if let amount = net.getAmountFromChargeHttpResponse(result: result) {
                                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                    self?.afpCardEntryVC?.finish(true)
                                }
                                // Show signature page
                                let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
                                // swiftlint:disable:next force_cast
                                let signatureVC = storyboard.instantiateViewController(withIdentifier: "SignatureViewController") as! SignatureViewController
                                signatureVC.chardId = chargeId
                                signatureVC.amount = amount
                                self!.afpCardEntryVC?.navigationController?.pushViewController(signatureVC, animated: false)
                                return
                            }
                        }
                    }

                    // Callback to signal card entry controller to finish
                    if let gwErrorMessage = net.getAllErrorMessagesFromChargeHttpResponse(result: result) {
                        self?.showAlertPopup(title: "AffiniPaySDK_Example: Charge Failed", message: gwErrorMessage, from: (self?.afpCardEntryVC)!, handler: { (_ alertAction) in
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                self?.afpCardEntryVC?.finish(false)
                            }
                        })
                    } else {
                        self?.showAlertPopup(title: "AffiniPaySDK_Example: Charge Failed", message: "Unknown reason", from: (self?.afpCardEntryVC)!, handler: { (_ alertAction) in
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                self?.afpCardEntryVC?.finish(false)
                            }
                        })
                    }
                    print("result \(String(describing: result))")
                    print("error \(String(describing: error))")
                })
            }
        }, onReset: {
            // callback on reset
            print("Block Called => onReset")
        }, onDismiss: {
            // callback on dismiss
            print("Block Called => onDismiss")
        }, onCancelCvvAlert: {
            // callback on dismiss
            print("Block Called => onCancelCvvAlert")
            self.afpCardEntryVC!.dismiss()
            self.transactionNav?.dismiss(animated: true, completion: {
                self.setOrientation()
            })
        })

        self.afpCardEntryVC?.title = "Charge"

        // Add cancel button
        let btnCancel = UIBarButtonItem(barButtonSystemItem: .cancel, target: nil, action: #selector(dismissCardEntry))
        btnCancel.tintColor = UIColor.white
        self.afpCardEntryVC?.navigationItem.rightBarButtonItem = btnCancel

        // Present card entry view controller
        fromVC.present(transactionNav!, animated: true, completion: nil)
        return self.afpCardEntryVC!
    }
    // MARK: - ClientDetailsControllerDelegate
    func onSubmitClientInfo(customerInfo: AFPCustomerInfo) {
        self.clientVC?.startLoading()
        let afpTokenizationCompleteParams = AFPTokenizationCompleteParams(customerInfo: customerInfo)
        self.afpCardEntryVC?.startTokenization(afpTokenizationCompleteParams!)
    }

    @objc func dismissCardEntry(sender: UIBarButtonItem) {
        self.dismissTransaction(sender: sender, from: self.afpCardEntryVC!)
    }

    @objc func dismissTransaction(sender: UIBarButtonItem, from: UIViewController, message: String? = nil) {
        DispatchQueue.main.async {
            let alertSuccess = UIAlertController(title: "Discard",
                                                 message: (message != nil) ? message: "Are you sure you want discard this payment and go back to Accounts?",
                                                 preferredStyle: UIAlertControllerStyle.alert)
            let okAction = UIAlertAction (title: "Discard",
                                          style: UIAlertActionStyle.default,
                                          handler: { [weak self] (_ alertAction) in
                                            self?.afpCardEntryVC!.dismiss()
                                            self?.transactionNav?.dismiss(animated: true, completion: {
                                                self?.setOrientation()
                                            })
                                        })
            let cancelAction = UIAlertAction (title: "Stay", style: UIAlertActionStyle.cancel, handler: nil)
            alertSuccess.addAction(okAction)
            alertSuccess.addAction(cancelAction)
            from.present(alertSuccess, animated: true, completion: nil)
        }
    }

}
