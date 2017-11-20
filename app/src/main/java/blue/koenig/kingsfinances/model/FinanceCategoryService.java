package blue.koenig.kingsfinances.model;

import com.koenig.commonModel.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 18.11.2017.
 */

class FinanceCategoryService implements CategoryService {

    private List<Category> categorys;

    @Override
    public boolean isValid(String newCategory) {
        if (categorys != null) {
            for (Category category : categorys) {
                if (category.getMain().equals(newCategory)) {
                    return false;
                }
                for (String sub : category.getSubs()) {
                    if (sub.equals(newCategory)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }



    @Override
    public List<String> getMainCategories() {
        if (categorys == null) {
            return new ArrayList<>();
        }

        List<String> mains = new ArrayList<>(categorys.size());
        for (Category category : categorys) {
            mains.add(category.getMain());
        }

        return mains;
    }

    @Override
    public List<String> getSubCategories(String mainCategory) {
        if (categorys == null) {
            return new ArrayList<>();
        }

        List<String> subs = new ArrayList<>(categorys.size());
        for (Category category : categorys) {
            if (category.getMain().equals(mainCategory)) {
                for (String sub : category.getSubs()) {
                    subs.add(sub);
                }
                break;
            }
        }

        return subs;
    }

    @Override
    public void addMainCategory(String newCategory) {
        // TODO: network call
        Category category = new Category(newCategory);
        categorys.add(category);
    }

    @Override
    public void addSubCategory(String mainCategory, String newCategory) {
// TODO: network
        for (Category category : categorys) {
            if (category.getMain().equals(mainCategory)) {
                category.addSub(newCategory);
            }
        }
    }

    @Override
    public void update(List<Category> categorys) {
        this.categorys = categorys;
    }
}
