package consulo.hub.backend.repository.repository;

import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
public interface RepositoryDownloadInfoRepository extends JpaRepository<RepositoryDownloadInfo, Long>
{
	List<RepositoryDownloadInfo> findAllByPluginId(String pluginId);
}
