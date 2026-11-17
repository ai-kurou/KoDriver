package kurou.kodriver.domain.model

public sealed class FeedbackType(
    public val tagValue: String,
) {
    public data object BugReport : FeedbackType("bug_report")

    public data object FeatureRequest : FeedbackType("feature_request")

    public data object Question : FeedbackType("question")

    public data object Other : FeedbackType("other")
}
