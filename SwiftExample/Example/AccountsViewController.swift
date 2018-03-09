import Foundation
import QuickTableViewController

class AccountsViewController: QuickTableViewController {
    // swiftlint:disable line_length

    private let refreshControl = UIRefreshControl()
    var selectedAccount: NSDictionary?
    var successBlock: (([String: Any]) -> Void)?
    var errorBlock: ((Error) -> Void)?
    var cancelBlock: (() -> Void)?
    var accounts: [String: Any]?

    // MARK: - Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Accounts"
        // Add Refresh Control to Table View
        if #available(iOS 10.0, *) {
            tableView.refreshControl = refreshControl
        } else {
            tableView.addSubview(refreshControl)
        }
        // Configure Refresh Control
        refreshControl.addTarget(self, action: #selector(refreshAccountData(_:)), for: .valueChanged)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        getAccounts(successBlock: { [weak self](dataDictionary) in
            DispatchQueue.main.async {
                self?.updateTable(dataDictionary)
            }
        }, errorBlock: { [weak self](_ error) in
                DispatchQueue.main.async {
                    self?.updateTable(nil)
                }
        })
    }

    // MARK: - Tableview
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = super.tableView(tableView, cellForRowAt: indexPath)
        if tableContents[indexPath.section].title == nil {
            // Alter the cells created by QuickTableViewController
            cell.imageView?.image = UIImage(named: "iconmonstr-x-mark")
        }
        return cell
    }

    func updateTable(_ dataDictionary: [String: Any]?) {
        var testRows: [Row & RowStyle] = []
        var liveRows: [Row & RowStyle] = []

        if dataDictionary == nil {
            var emptyRow: [Row & RowStyle] = []
            emptyRow.append(NavigationRow(title: "Server Error", subtitle: .leftAligned("Fix and refresh table")))
            self.tableContents = [ Section(title: "", rows: emptyRow) ]
            return
        }

        for (keyString, value) in dataDictionary! {
            if keyString == "test_accounts" || keyString == "live_accounts" {
                guard let valueArray = value as? NSArray else { continue }
                for account in valueArray {
                    guard let accountDict = account as? [String: Any] else { continue }
                    let leftText = accountDict["name"] as? String ?? ""
                    let row = TapActionRow<TapActionCell>(title: leftText, action: { [weak self](row) in
                        self?.accountSelected(row, accountDict)
                        print("account selected")
                    })

                    switch keyString {
                    case "test_accounts":
                        testRows.append(row)
                    case "live_accounts":
                        liveRows.append(row)
                    default:
                        break
                    }
                }
            }
        }

        // Show empty results
        if testRows.count == 0 && liveRows.count == 0 {
            var emptyRow: [Row & RowStyle] = []
            emptyRow.append(NavigationRow(title: "No Accounts", subtitle: .leftAligned("Fix and refresh table")))
            self.tableContents = [ Section(title: "", rows: emptyRow) ]
            return
        }

        // Show results
        self.tableContents = [
            Section(title: "Test", rows: testRows),
            Section(title: "Live", rows: liveRows)
        ]
    }

    // MARK: - Accounts
    private func getAccounts(successBlock: (([String: Any]) -> Void)? = nil, errorBlock: ((Error) -> Void)? = nil) {
        let net: NetworkController = NetworkControllerImpl()
        net.apiGet("/api/v1/merchant") { (data, _ response, error) in
            if let err = error {
                errorBlock?(err)
            }
            guard let data = data else {
                return
            }
            guard let json = try? JSONSerialization.jsonObject(with: data, options: .mutableContainers) else {
                return
            }
            guard let jsonDict = json as? [String: Any] else {
                print("Result is not a JSON NSDictionary.")
                return
            }

            successBlock?(jsonDict)
        }
    }

    @objc private func refreshAccountData(_ sender: Any) {
        getAccounts(successBlock: { [weak self](dataDictionary) in
            DispatchQueue.main.async {
                self?.updateTable(dataDictionary)
                self?.refreshControl.endRefreshing()
            }
        }, errorBlock: { [weak self](_ error) in
            DispatchQueue.main.async {
                self?.refreshControl.endRefreshing()
                self?.updateTable(nil)
            }
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + .milliseconds(500)) {
                self?.showAlertPopup(title: "Error", message: String(describing: error))
            }
        })
    }

    private func accountSelected(_ sender: Row, _ account: [String: Any]) {
        self.successBlock?(account)
    }

    // MARK: - Utilities
    func showAlertPopup(title: String, message: String) {
        let alertSuccess = UIAlertController(title: title, message: message, preferredStyle: UIAlertControllerStyle.alert)
        let okAction = UIAlertAction (title: "OK", style: UIAlertActionStyle.cancel, handler: nil)
        alertSuccess.addAction(okAction)
        self.present(alertSuccess, animated: true, completion: nil)
    }

    private func showAlert(_ sender: Row) {
        let alert = UIAlertController(title: "Action Triggered", message: nil, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .cancel) { [unowned self] _ in
            self.dismiss(animated: true, completion: nil)
        })
        present(alert, animated: true, completion: nil)
    }

    private func showDetail(_ sender: Row) {
        let controller = UIViewController()
        controller.view.backgroundColor = UIColor.white
        controller.title = "\(sender.title) " + (sender.subtitle?.text ?? "")
        navigationController?.pushViewController(controller, animated: true)
    }

    private func printValue(_ sender: Row) {
        if let row = sender as? SwitchRow {
            print("\(row.title) = \(row.switchValue)")
        }
    }
}
