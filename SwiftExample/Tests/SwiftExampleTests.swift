import Quick
import Nimble
import AffiniPaySDK
import QuickTableViewController

class Tests: QuickSpec {
    // swiftlint:disable line_length
    // swiftlint:disable function_body_length
    override func spec() {
        describe("Base server URL") {
            it("reads from plist") {
                var myDict: NSDictionary?
                var url: String?
                let bundle = Bundle(for: type(of: self))
                if let path = bundle.path(forResource: "Info", ofType: "plist") {
                    myDict = NSDictionary(contentsOfFile: path)
                }
                if let dict = myDict {
                    if let affinipaySdkBaseUrl = dict["affinipaySdkBaseUrl"] as? String {
                        url = affinipaySdkBaseUrl
                    }
                }
                expect(url != nil) == true
            }
        }

        describe("AFPChargeResult") {
            it("instantiates") {
                let amountVC: Any? = AFPAmountVC()
                expect(amountVC != nil) == true
            }
        }

        describe("AFPCardEntryVC") {
            it("instantiates") {
                let cardEntryVC: Any? = AFPCardEntryVC()
                expect(cardEntryVC != nil) == true
            }
        }

        describe("Merchant info") {
            it ("gets") {
                let net: NetworkController = NetworkControllerImpl()
                waitUntil(timeout: 10) { done in
                    net.apiGet("/api/v1/merchant") { (data, _ response, error) in
                        expect(error == nil) == true
                        expect(data != nil) == true
                        let json = try? JSONSerialization.jsonObject(with: data!, options: .mutableContainers)
                        expect(json != nil) == true
                        print("merchant info: \(json!)")
                        let jsonDict = json as? [String: Any]
                        expect(jsonDict != nil) == true

                        done()
                    }
                }
            }
        }

        describe("AFPChargeInput") {
            it("initializes and reads") {
                let name = "AffiniPay"
                let address1 = "3700 N Capital of Texas Hwy #300"
                let city = "Austin"
                let state = "Texas"
                let postalCode = "78704"
                let publicKey = NSUUID().uuidString
                let amount = "101"
                let customerInfo: AFPCustomerInfoResult = AFPCustomerInfoResult()
                customerInfo.customerInfo = AFPCustomerInfo()
                customerInfo.customerInfo.name = name
                customerInfo.customerInfo.address1 = address1
                customerInfo.customerInfo.city = city
                customerInfo.customerInfo.state = state
                customerInfo.customerInfo.postalCode = postalCode
                let chargeInput: AFPChargeParams =
                    AFPChargeParams(publicKey: publicKey,
                                   amount: amount,
                                   customerInfo: customerInfo.customerInfo)
                expect(chargeInput.publicKey) == publicKey
                expect(chargeInput.amount) == amount
                expect(chargeInput.customerInfo != nil) == true
                expect(chargeInput.customerInfo.name) == name
                expect(chargeInput.customerInfo.address1) == address1
                expect(chargeInput.customerInfo.city) == city
                expect(chargeInput.customerInfo.state) == state
                expect(chargeInput.customerInfo.postalCode) == postalCode
            }
        }

        describe("AFPChargeResult") {
            it("initializes and reads") {
                let oneTimeToken = NSUUID().uuidString
                let accountId = NSUUID().uuidString
                let amount = "102"
                let posFields: [String: String] = ["input_capabilities": "CHIP,SWIPE,KEY",
                                                   "cardholder_verification_method": "SIGNATURE_MANUAL_VERIFICATION",
                                                   "operating_environment": "MPOS_ON_ACCEPTOR_PREMISES"]
                let paymentDataSource = "ACRNM"
                let chargeResult: AFPChargeResult? =
                    AFPChargeResult(oneTimeToken: oneTimeToken,
                                    accountId: accountId,
                                    amount: amount, posFields:
                        posFields, paymentDataSource: paymentDataSource)
                expect(chargeResult != nil) == true
                expect(chargeResult!.oneTimeToken) == oneTimeToken
                expect(chargeResult!.accountId) == accountId
                expect(chargeResult!.amount) == amount
                if let responsePosFields = chargeResult!.posFields as? [String: String] {
                    expect(responsePosFields["input_capabilities"]) == posFields["input_capabilities"]
                    expect(responsePosFields["cardholder_verification_method"]) == posFields["cardholder_verification_method"]
                    expect(responsePosFields["operating_environment"]) == posFields["operating_environment"]
                } else {
                    expect("posFields") == "found in chrge response"
                }
                expect(chargeResult!.paymentDataSource) == paymentDataSource
            }
        }

    }
}
