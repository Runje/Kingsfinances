package blue.koenig.kingsfinances.view;

import com.koenig.commonModel.Category;

import java.util.List;

/**
 * Created by Thomas on 17.11.2017.
 */

interface PickCategoryInteractor {
    void onPick(String mainCategory, String subCategory);

    List<String> getSubs(String item);

    boolean isValid(String category);

    void addMainCategory(String newCategory);
    void addSubCategory(String mainCategory, String newCategory);

    List<String> getMainCategorys();
}
