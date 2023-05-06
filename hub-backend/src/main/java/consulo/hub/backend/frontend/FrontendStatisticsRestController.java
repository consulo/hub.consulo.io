package consulo.hub.backend.frontend;

import consulo.hub.backend.auth.UserAccountService;
import consulo.hub.backend.statistics.repository.StatisticEntryRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 07/09/2021
 */
@RestController
public class FrontendStatisticsRestController
{
	@Autowired
	private StatisticEntryRepository myStatisticEntryRepository;

	@Autowired
	private UserAccountService myUserAccountService;

	@RequestMapping("/api/private/statistics/list")
	public List<StatisticEntry> listStatistics(@RequestParam(value = "userId", required = false) Long userId)
	{
		if(userId == null)
		{
			return myStatisticEntryRepository.findAll(Sort.by(Sort.Direction.ASC, "createTime"));
		}

		UserAccount user = Objects.requireNonNull(myUserAccountService.findUser(userId));
		return myStatisticEntryRepository.findByUser(user, Sort.by(Sort.Direction.ASC, "createTime"));
	}
}
