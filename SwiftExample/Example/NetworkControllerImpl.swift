import Foundation

public class NetworkControllerImpl: NetworkController {
    // swiftlint:disable line_length

    // MARK: - Url base
    func getUrlBase() -> String {
        var myDict: NSDictionary?
        let bundle = Bundle(for: type(of: self))
        if let path = bundle.path(forResource: "Info", ofType: "plist") {
            myDict = NSDictionary(contentsOfFile: path)
        }
        if let dict = myDict {
            if let affinipaySdkBaseUrl = dict["affinipaySdkBaseUrl"] as? String {
                return affinipaySdkBaseUrl
            }
        }
        return ""
    }

    func getGatewayBase() -> String? {
        var myDict: NSDictionary?
        let bundle = Bundle(for: type(of: self))
        if let path = bundle.path(forResource: "Info", ofType: "plist") {
            myDict = NSDictionary(contentsOfFile: path)
        }
        if let dict = myDict {
            if let affinipayGatewayUrl = dict["affinipayGatewayUrl"] as? String {
                return affinipayGatewayUrl
            }
        }
        return nil
    }
    
    // MARK: - Network calls
    func createCharge(_ chargetokenId: String?, _ accountId: String?, _ amount: Int, _ completion: @escaping ((_ result: [String: Any]?, _ error: Error?) -> Void)) {
        let url = URL(string: getUrlBase() + "/api/v1/charge")!
        var request = URLRequest(url: url)
        let configuration = URLSessionConfiguration.ephemeral
        let session = URLSession(configuration: configuration, delegate: nil, delegateQueue: OperationQueue.main)
        request.httpMethod = "POST"
        request.setValue("000", forHTTPHeaderField: "x-afp-application-id")
        request.setValue("000", forHTTPHeaderField: "x-afp-client-key")
        request.setValue("000", forHTTPHeaderField: "x-afp-public-key")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        request.addValue("affinipay sdk", forHTTPHeaderField: "Referer")

        let chargeData: [String: Any] = [
            "account_id": accountId!,
            "token_id": chargetokenId!,
            "amount": String(amount),
            "source_id": "affinipaysample://\(NSUUID().uuidString)"
        ]

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: chargeData, options: [])
        } catch {
            request.httpBody = nil
        }

        let task = (session as URLSession).dataTask(with: request) { (data, _ response, error) in
            if error != nil {
                DispatchQueue.main.async {
                    completion(nil, error)
                }
            }

            if let dataValue = data {
                let responseData = String(data: dataValue, encoding: String.Encoding.utf8)
                print("Charge response data: \(responseData!)")
                do {
                    // Convert the data to JSON
                    let jsonSerialized = try JSONSerialization.jsonObject(with: dataValue, options: []) as? [String: Any]
                    DispatchQueue.main.async {
                        completion(jsonSerialized, nil)
                   }
                } catch let error as NSError {
                    print(error.localizedDescription)
                    DispatchQueue.main.async {
                        completion(nil, error)
                    }
                }
            } else {
                print("No data from charge")
                DispatchQueue.main.async {
                    completion(nil, NSError(domain: "", code: 404, userInfo: nil))
                }
            }
        }
        task.resume()
    }

    func signCharge(_ chargeId: String?, _ accountId: String?, _ signatureData: String?, _ completion: @escaping ((_ result: [String: Any]?, _ error: Error?) -> Void)) {
        let url = URL(string: getUrlBase() + "/api/v1/sign")!
        var request = URLRequest(url: url)
        let configuration = URLSessionConfiguration.ephemeral
        let session = URLSession(configuration: configuration, delegate: nil, delegateQueue: OperationQueue.main)
        request.httpMethod = "POST"
        request.setValue("000", forHTTPHeaderField: "x-afp-application-id")
        request.setValue("000", forHTTPHeaderField: "x-afp-client-key")
        request.setValue("000", forHTTPHeaderField: "x-afp-public-key")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        request.addValue("affinipay sdk", forHTTPHeaderField: "Referer")

        let chargeData: [String: Any] = [
            "charge_id": chargeId!,
            "account_id": accountId!,
            "data": signatureData!
        ]

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: chargeData, options: [])
        } catch {
            request.httpBody = nil
        }

        let task = (session as URLSession).dataTask(with: request) { (data, _ response, error) in
            if error != nil {
                DispatchQueue.main.async {
                    completion(nil, error)
                }
            }
            if let dataValue = data {
                let responseData = String(data: dataValue, encoding: String.Encoding.utf8)
                print("Sign response data: \(responseData!)")
                do {
                    // Convert the data to JSON
                    let jsonSerialized = try JSONSerialization.jsonObject(with: dataValue, options: []) as? [String: Any]
                    DispatchQueue.main.async {
                        completion(jsonSerialized, nil)
                    }
                } catch let error as NSError {
                    print(error.localizedDescription)
                    DispatchQueue.main.async {
                        completion(nil, error)
                    }
                }
            } else {
                print("No data from sign")
                DispatchQueue.main.async {
                    completion(nil, NSError(domain: "", code: 404, userInfo: nil))
                }
            }
        }
        task.resume()
    }

    func apiGet(_ endpoint: String, _ completionHandler: @escaping (Data?, URLResponse?, Error?) -> Void ) {
        let url = URL(string: getUrlBase() + endpoint)
        let ephemeralConfiguration = URLSessionConfiguration.ephemeral
        let ephemeralSession = URLSession(configuration: ephemeralConfiguration)
        var ephemeralRequest = URLRequest(url: url!)
        ephemeralRequest.setValue("000", forHTTPHeaderField: "X_AFP_APPLICATION_ID")
        ephemeralRequest.setValue("000", forHTTPHeaderField: "X_AFP_CLIENT_KEY")
        let task = ephemeralSession.dataTask(with: ephemeralRequest) { (data, response, error) in
            DispatchQueue.main.async {
                completionHandler(data, response, error)
            }
        }

        task.resume()
    }

    func apiPost(_ endpoint: String, _ publicKey: String, _ completionHandler: @escaping (Data?, URLResponse?, Error?) -> Void ) {
        let url = URL(string: getUrlBase() + endpoint)
        let ephemeralConfiguration = URLSessionConfiguration.ephemeral
        let ephemeralSession = URLSession(configuration: ephemeralConfiguration)
        var ephemeralRequest = URLRequest(url: url!)
        ephemeralRequest.setValue("000", forHTTPHeaderField: "X_AFP_APPLICATION_ID")
        ephemeralRequest.setValue("000", forHTTPHeaderField: "X_AFP_CLIENT_KEY")
        ephemeralRequest.setValue(publicKey, forHTTPHeaderField: "X_AFP_PUBLIC_KEY")
        let task = ephemeralSession.dataTask(with: ephemeralRequest) { (data, response, error) in
            DispatchQueue.main.async {
                completionHandler(data, response, error)
            }
        }

        task.resume()
    }

    // MARK: - Parse responses
    func JSONStringify(value: Any, prettyPrinted: Bool = false) -> String {
        if JSONSerialization.isValidJSONObject(value) {
            do {
                let data = try JSONSerialization.data(withJSONObject: value, options: JSONSerialization.WritingOptions.prettyPrinted)
                if let string = NSString(data: data, encoding: String.Encoding.utf8.rawValue) {
                    return string as String
                }
            } catch {
                print(error)
            }
        }
        return ""
    }

    func getChargeIdFromChargeHttpResponse(result: [String: Any]?) -> String? {
        if result != nil {
            if let attributes = result!["attributes"] as? [String: Any] {
                if let identifier = attributes["id"] as? String {
                    return identifier
                }
            }
        }
        return nil
    }

    func getAmountFromChargeHttpResponse(result: [String: Any]?) -> Int? {
        if result != nil {
            if let attributes = result!["attributes"] as? [String: Any] {
                if let amount = attributes["amount"] as? Int {
                    return amount
                }
            }
        }
        return nil
    }

    func getCreatedAtFromChargeHttpResponse(result: [String: Any]?) -> String? {
        if result != nil {
            if let attributes = result!["attributes"] as? [String: Any] {
                if let created = attributes["created"] as? String {
                    return created
                }
            }
        }
        return nil
    }

    func getStatusFromChargeHttpResponse(result: [String: Any]?) -> String? {
        if result != nil {
            if let attributes = result!["attributes"] as? [String: Any] {
                if let status = attributes["status"] as? String {
                    return status
                }
            }
        }
        return nil
    }

    func getAuthorizationCodeFromChargeHttpResponse(result: [String: Any]?) -> String? {
        if result != nil {
            if let attributes = result!["attributes"] as? [String: Any] {
                if let authorizationCode = attributes["authorization_code"] as? String {
                    return authorizationCode
                }
            }
        }
        return nil
    }

    func getAccountIdFromChargeHttpResponse(result: [String: Any]?) -> String? {
        if result != nil {
            if let attributes = result!["attributes"] as? [String: Any] {
                if let accountId = attributes["account_id"] as? String {
                    return accountId
                }
            }
        }
        return nil
    }

    func getErrorsFromChargeHttpResponse(result: [String: Any]?) -> [String: Any]? {
        if result != nil {
            if let errors = result!["errors"] as? [String: Any] {
                return errors
            }
        }
        return nil
    }

    func getAllErrorMessagesFromChargeHttpResponse(result: [String: Any]?) -> String? {
        var message: String? = nil
        if let errors = self.getErrorsFromChargeHttpResponse(result: result) {
            for (_, value) in errors {
                if let valueString = value as? String {
                    if message == nil { message = valueString } else { message = "\(message!)\n\(valueString)" }
                } else if let valueArray = value as? [Any] {
                    for valueAny in valueArray {
                        if let valueString = valueAny as? String {
                            if message == nil { message = valueString } else { message = "\(message!)\n\(valueString)"}
                        }
                    }
                }
            }
        }
        return message
    }

    func getChargeResponse(result: [String: Any]?) -> [String: Any] {
        var output: [String: Any] = [:]

        let chargeId = self.getChargeIdFromChargeHttpResponse(result: result)
        let authorizedAmount = self.getAmountFromChargeHttpResponse(result: result)
        let createdAt = self.getCreatedAtFromChargeHttpResponse(result: result)
        let status = self.getStatusFromChargeHttpResponse(result: result)
        let authorizationCode = self.getAuthorizationCodeFromChargeHttpResponse(result: result)

        if chargeId != nil && authorizedAmount != nil && createdAt != nil && status != nil && authorizationCode != nil {
            output["success"] = "ID: \(chargeId!)\n Authorized Amount: \(authorizedAmount!)\n Created At: \(createdAt!)\n Status: \(status!)\n Authorization Code: \(authorizationCode!)"
            return output
        }

        if let errors = self.getAllErrorMessagesFromChargeHttpResponse(result: result) {
            output["error"] = errors
            return output
        }
        output["error"] = "Unknown error"
        return output
    }
}
