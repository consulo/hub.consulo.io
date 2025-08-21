package consulo.review.backend.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2025-05-19
 */
@RestController
public class ReviewRestController {
    @GetMapping("/api/auth/login")
    public Object apiAuthLogin(@RequestParam("userName") String userName, @RequestParam("password") String password) {
        return Map.of("token", password);
    }

    @GetMapping("/api/review/repositories")
    public Object apiReviewRepositories() {
        Repositories repositories = new Repositories();
        repositories.repoData = new RepoRaw[0];
        return repositories;
    }

    @GetMapping("/api/review/{id}/details")
    public Object apiReviewDetails(@PathVariable("id") String id) {
        return createReview("requireMyApproval");
    }

    @GetMapping("/api/review/listByState/{state}")
    public Object apiReviewListByState(@PathVariable("state") String state) {
        Reviews reviews = new Reviews();

        reviews.reviewData = new ReviewRaw[]{
            createReview(state)
        };
        return reviews;
    }

    private ReviewRaw createReview(String state) {
        ReviewRaw r = new ReviewRaw();

        UserRaw userRaw = new UserRaw();
        userRaw.userName = "vistall.valeriy@gmail.com";
        userRaw.displayName = "Valery Semenchuk";
        userRaw.avatarUrl = "https://gravatar.com/avatar/017d6e09fe36131ab314cbbd08c45e93?s=400&d=identicon&r=x";

        r.author = userRaw;
        r.moderator = userRaw;
        r.creator = userRaw;
        r.permaId = new IdHolder();
        r.permaId.id = "5";

        r.name = "Test";
        r.description = "Some Test Description";
        r.state = state;

        r.projectKey = "TEST";
        r.createDate = new Date();

        return r;
    }
}
