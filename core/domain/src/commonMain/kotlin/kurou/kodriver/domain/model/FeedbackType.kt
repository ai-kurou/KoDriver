package kurou.kodriver.domain.model

sealed class FeedbackType(
    val tagValue: String,
) {
    data object BugReport : FeedbackType("bug_report")

    data object FeatureRequest : FeedbackType("feature_request")

    data object Question : FeedbackType("question")

    data object Other : FeedbackType("other")
}
