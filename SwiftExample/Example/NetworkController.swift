import Foundation

protocol NetworkController {
    // swiftlint:disable line_length

    func apiPost(_ endpoint: String, _ publicKey: String, _ completionHandler: @escaping (Data?, URLResponse?, Error?) -> Void )

    func apiGet(_ endpoint: String, _ completionHandler: @escaping (Data?, URLResponse?, Error?) -> Void )

    func createCharge(_ chargetokenId: String?, _ accountId: String?, _ amount: Int,
                      _ completion: @escaping ((_ result: [String: Any]?, _ error: Error?) -> Void))

    func signCharge(_ chargeId: String?, _ accountId: String?, _ signatureData: String?,
                    _ completion: @escaping ((_ result: [String: Any]?, _ error: Error?) -> Void))

    func getChargeIdFromChargeHttpResponse(result: [String: Any]?) -> String?

    func getAmountFromChargeHttpResponse(result: [String: Any]?) -> Int?

    func getCreatedAtFromChargeHttpResponse(result: [String: Any]?) -> String?

    func getStatusFromChargeHttpResponse(result: [String: Any]?) -> String?

    func getAuthorizationCodeFromChargeHttpResponse(result: [String: Any]?) -> String?

    func getAccountIdFromChargeHttpResponse(result: [String: Any]?) -> String?

    func getErrorsFromChargeHttpResponse(result: [String: Any]?) -> [String: Any]?

    func getAllErrorMessagesFromChargeHttpResponse(result: [String: Any]?) -> String?

    func getChargeResponse(result: [String: Any]?) -> [String: Any]
}
