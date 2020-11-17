//
//  SignatureViewController.swift
//  AffiniPaySDK_Example
//
//  Created by minhazpanara on 13/02/20.
//  Copyright © 2020 CocoaPods. All rights reserved.
//

import UIKit

class SignatureViewController: UIViewController {

    public var chardId: String?
    public var amount: Int?
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }
    @IBAction func onDoneButtonAction(_ sender: UIButton) {
        self.navigationController?.popToRootViewController(animated: true)
    }
}
