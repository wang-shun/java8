package io.terminus.doctor.web.front.role;

import com.google.common.base.Splitter;
import io.terminus.doctor.web.core.auth.AuthLoader;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.parana.auth.CompiledTree;
import io.terminus.parana.auth.parser.ParseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Effet
 */
@RestController
@RequestMapping("/api/role/tree")
public class RoleTrees {

    private final AuthLoader authLoader;

    @Autowired
    public RoleTrees(AuthLoader authLoader) {
        this.authLoader = authLoader;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<CompiledTree> getTreesByScopes(@RequestParam String scopes) {
        List<String> scopeList = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(scopes);

        ParseResult result = authLoader.getTree(ThreadVars.getApp());

        List<CompiledTree> trees = new ArrayList<>();
        for (CompiledTree compiledTree : result.getCompiledTrees()) {
            if (scopeList.contains(compiledTree.getScope())) {
                trees.add(compiledTree);
            }
        }
        return trees;
    }
}
