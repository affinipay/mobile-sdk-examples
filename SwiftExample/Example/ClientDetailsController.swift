//
//  ClientDetailsController.swift
//  AffiniPaySDK_Example
//
//  Created by minhazpanara on 12/02/20.
//  Copyright © 2020 CocoaPods. All rights reserved.
//

import UIKit
import AffiniPaySDK.AFPCustomerInfo
protocol ClientDetailsControllerDelegate {
    func onSubmitClientInfo(customerInfo: AFPCustomerInfo)
}
class ClientDetailsController: UIViewController {
    var delegate: ClientDetailsControllerDelegate!
    static let sharedInstance = ClientDetailsController()
    @IBOutlet weak var loadingActivityIndicator: UIActivityIndicatorView!
    static func clientDetailsVC() -> ClientDetailsController {
        let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
        // swiftlint:disable:next force_cast
        let clientDetailsVC = storyboard.instantiateViewController(withIdentifier: "ClientDetailsController") as! ClientDetailsController
        return clientDetailsVC
    }
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    @IBAction func onClientDetailsSubmit(_ sender: UIButton) {
        let customerInfo = self.getCustomerInfoInput()
        self.delegate.onSubmitClientInfo(customerInfo: customerInfo!)
    }

    @IBAction func onBackAction(_ sender: UIButton) {
        self.navigationController?.popViewController(animated: true)
    }
    
    func getCustomerInfoInput() -> AFPCustomerInfo? {
        let inputValues = AFPCustomerInfo()
        inputValues.email = "iossdk@affinipay.com"
        inputValues.phone = "512-555-1212"
        inputValues.reference = "iOS SDK sample app"
        inputValues.city = "Austin"
        inputValues.state = "Texas"
        inputValues.country = "US"
        inputValues.name = "Max Payne"
        inputValues.address1 = "12"
        inputValues.address2 = "xyz"
        inputValues.postalCode = "12123"
        return inputValues
    }
    
    func startLoading() {
        loadingActivityIndicator.startAnimating()
    }
    
    func stopLoading() {
        loadingActivityIndicator.stopAnimating()
    }
}
