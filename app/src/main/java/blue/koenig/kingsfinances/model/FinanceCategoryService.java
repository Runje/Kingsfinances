package blue.koenig.kingsfinances.model;

import com.koenig.commonModel.Category;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection;
import blue.koenig.kingsfinances.model.database.CategoryTable;

/**
 * Created by Thomas on 18.11.2017.
 */

class FinanceCategoryService implements CategoryService {
    private List<Category> categorys;
    private CategoryServiceListener listener;

    public void setListener(CategoryServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isValid(String newCategory) {
        if (categorys != null) {
            for (Category category : categorys) {
                if (category.getName().equals(newCategory)) {
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
            mains.add(category.getName());
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
            if (category.getName().equals(mainCategory)) {
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
        Category category = new Category(newCategory);
        categorys.add(category);

        if (listener != null) {
            listener.addMainCategory(newCategory);
        }
    }

    @Override
    public void addSubCategory(String mainCategory, String newCategory) {
        for (Category category : categorys) {
            if (category.getName().equals(mainCategory)) {
                category.addSub(newCategory);
            }
        }

        if (listener != null) {
            listener.addSubCategory(mainCategory, newCategory);
        }
    }

    @Override
    public void update(List<Category> categorys) {
        this.categorys = categorys;
    }

    public interface CategoryServiceListener {

        void addMainCategory(String newCategory);

        void addSubCategory(String mainCategory, String newCategory);
    }
}
