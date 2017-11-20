package blue.koenig.kingsfinances.model;

import com.koenig.commonModel.Category;

import java.util.List;

/**
 * Created by Thomas on 17.11.2017.
 */

public interface CategoryService {

    /**
     * Check if new service is valid(not empty) and if the category not exists yet.
     * Check only on local repository, no network request!
     * @param newCategory
     * @return
     */
    boolean isValid(String newCategory);

    List<String> getMainCategories();

    List<String> getSubCategories(String mainCategory);

    void addMainCategory(String newCategory);
    void addSubCategory(String mainCategory, String newCategory);

    void update(List<Category> categorys);
}
