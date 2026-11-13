import KMP
import UIKit.UIDevice

// Kotlin reads these from its background threads, as network calls and use cases run there, so none of them may touch
// the main actor: Bundle, ProcessInfo, uname and Locale are safe off it, UIDevice isn't.
final class PlatformImpl: Platform {

    // The same name as on Android; the bundle name has no space
    nonisolated var appName: String {
        MR.strings().app_name.desc().localized()
    }

    nonisolated var appVersionName: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown"
    }

    nonisolated var appBuildNumber: String {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "Unknown"
    }

    nonisolated var applicationId: String {
        Bundle.main.bundleIdentifier ?? "Unknown"
    }

    nonisolated var deviceModel: String {
        var sysinfo = utsname()
        uname(&sysinfo)
        return String(bytes: Data(bytes: &sysinfo.machine, count: Int(_SYS_NAMELEN)), encoding: .ascii)?.trimmingCharacters(in: .controlCharacters) ?? "Unknown"
    }

    nonisolated var storeReviewUrl: String {
        "https://apps.apple.com/app/id\(AppStore.appId)?action=write-review"
    }

    // "iOS 26.1", as UIDevice's systemName and systemVersion read
    nonisolated var osNameWithVersion: String {
        let version = ProcessInfo.processInfo.operatingSystemVersion
        let patch = version.patchVersion > 0 ? ".\(version.patchVersion)" : ""
        return "iOS \(version.majorVersion).\(version.minorVersion)\(patch)"
    }

    nonisolated var preferredLanguages: [String] {
        Locale.preferredLanguages
    }

    nonisolated var regionCode: String? {
        Locale.current.region?.identifier
    }

    nonisolated func countryName(code: String) -> String? {
        Locale(identifier: "en").localizedString(forRegionCode: code)
    }
}
