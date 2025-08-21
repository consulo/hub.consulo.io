package consulo.review.backend.rest;

import java.util.Date;

/**
 * @author VISTALL
 * @since 2025-05-19
 */
public class ReviewRaw {
    public String projectKey;
    public String name;
    public String description;
    public UserRaw author;
    public UserRaw moderator;
    public UserRaw creator;
    public IdHolder permaId;
    public String[] permaIdHistory;
    public String state;
    public String type;
    public boolean allowReviewersToJoin;
    public int metricsVersion;
    public Date createDate;
    public Date dueDate;
    public Reviewers reviewers;
    public ReviewItems reviewItems;
    public GeneralComments generalComments;
    public VersionedComments versionedComments;
    public Transitions transitions;
    public Actions actions;
    public Stat[] stats;
}
