package consulo.review.backend.rest;

import org.springframework.data.history.Revision;

/**
 * @author VISTALL
 * @since 2025-05-19
 */
public class ReviewItemRaw {
    public IdHolder permId;
    public Participant[] participants;
    public String repositoryName;
    public String fromPath;
    public String fromRevision;
    public String fromContentUrl;
    public String toPath;
    public String toRevision;
    public String toContentUrl;
    public String patchUrl;
    public String fileType;
    public String commitType;
    public String authorName;
    public boolean showAsDiff;
    public long commitDate;
    public Revision[] expandedRevisions;
}
