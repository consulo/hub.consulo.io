package consulo.hub.backend.repository.external.homebrew;

import consulo.hub.shared.repository.PluginChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Serves Homebrew formula files (.rb) for Consulo plugins and macOS platform packages.
 *
 * @author VISTALL
 */
@RestController
@RequestMapping("/api/homebrew")
public class HomebrewRepositoryController {

    private final HomebrewFormulaStore myFormulaStore;

    @Autowired
    public HomebrewRepositoryController(HomebrewFormulaStore formulaStore) {
        myFormulaStore = formulaStore;
    }

    @GetMapping(value = "/{channel}/formula/consulo-plugin-{pluginId}.rb",
                produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> pluginFormula(@PathVariable PluginChannel channel,
                                                @PathVariable String pluginId) {
        String content = myFormulaStore.getPluginFormula(channel, pluginId);
        if (content == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header("Content-Type", "text/plain; charset=utf-8").body(content);
    }

    @GetMapping(value = "/{channel}/formula/{formulaName}.rb",
                produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> platformFormula(@PathVariable PluginChannel channel,
                                                  @PathVariable String formulaName) {
        if (!formulaName.equals("consulo-with-jdk") && !formulaName.equals("consulo-without-jdk")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String content = myFormulaStore.getPlatformFormula(channel, formulaName);
        if (content == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header("Content-Type", "text/plain; charset=utf-8").body(content);
    }
}
