package org.docksidestage.handson.dbflute.bsentity;

import java.util.List;
import java.util.ArrayList;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.AbstractEntity;
import org.dbflute.dbmeta.accessory.DomainEntity;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.allcommon.DBMetaInstanceHandler;
import org.docksidestage.handson.dbflute.exentity.*;

/**
 * The entity of (商品カテゴリ)PRODUCT_CATEGORY as TABLE. <br>
 * 商品のカテゴリを表現するマスタ。<br>
 * 自己参照の階層になっている。
 * @author DBFlute(AutoGenerator)
 */
public abstract class BsProductCategory extends AbstractEntity implements DomainEntity {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** PRODUCT_CATEGORY_CODE: {PK, NotNull, CHAR(3)} */
    protected String _productCategoryCode;

    /** PRODUCT_CATEGORY_NAME: {NotNull, VARCHAR(50)} */
    protected String _productCategoryName;

    /** (親カテゴリコード)PARENT_CATEGORY_CODE: {IX, CHAR(3), FK to product_category} */
    protected String _parentCategoryCode;

    // ===================================================================================
    //                                                                             DB Meta
    //                                                                             =======
    /** {@inheritDoc} */
    public DBMeta asDBMeta() {
        return DBMetaInstanceHandler.findDBMeta(asTableDbName());
    }

    /** {@inheritDoc} */
    public String asTableDbName() {
        return "product_category";
    }

    // ===================================================================================
    //                                                                        Key Handling
    //                                                                        ============
    /** {@inheritDoc} */
    public boolean hasPrimaryKeyValue() {
        if (_productCategoryCode == null) { return false; }
        return true;
    }

    // ===================================================================================
    //                                                                    Foreign Property
    //                                                                    ================
    /** (商品カテゴリ)PRODUCT_CATEGORY by my PARENT_CATEGORY_CODE, named 'productCategorySelf'. */
    protected OptionalEntity<ProductCategory> _productCategorySelf;

    /**
     * [get] (商品カテゴリ)PRODUCT_CATEGORY by my PARENT_CATEGORY_CODE, named 'productCategorySelf'. <br>
     * Optional: alwaysPresent(), ifPresent().orElse(), get(), ...
     * @return The entity of foreign property 'productCategorySelf'. (NotNull, EmptyAllowed: when e.g. null FK column, no setupSelect)
     */
    public OptionalEntity<ProductCategory> getProductCategorySelf() {
        if (_productCategorySelf == null) { _productCategorySelf = OptionalEntity.relationEmpty(this, "productCategorySelf"); }
        return _productCategorySelf;
    }

    /**
     * [set] (商品カテゴリ)PRODUCT_CATEGORY by my PARENT_CATEGORY_CODE, named 'productCategorySelf'.
     * @param productCategorySelf The entity of foreign property 'productCategorySelf'. (NullAllowed)
     */
    public void setProductCategorySelf(OptionalEntity<ProductCategory> productCategorySelf) {
        _productCategorySelf = productCategorySelf;
    }

    // ===================================================================================
    //                                                                   Referrer Property
    //                                                                   =================
    /** PRODUCT by PRODUCT_CATEGORY_CODE, named 'productList'. */
    protected List<Product> _productList;

    /**
     * [get] PRODUCT by PRODUCT_CATEGORY_CODE, named 'productList'.
     * @return The entity list of referrer property 'productList'. (NotNull: even if no loading, returns empty list)
     */
    public List<Product> getProductList() {
        if (_productList == null) { _productList = newReferrerList(); }
        return _productList;
    }

    /**
     * [set] PRODUCT by PRODUCT_CATEGORY_CODE, named 'productList'.
     * @param productList The entity list of referrer property 'productList'. (NullAllowed)
     */
    public void setProductList(List<Product> productList) {
        _productList = productList;
    }

    /** (商品カテゴリ)PRODUCT_CATEGORY by PARENT_CATEGORY_CODE, named 'productCategorySelfList'. */
    protected List<ProductCategory> _productCategorySelfList;

    /**
     * [get] (商品カテゴリ)PRODUCT_CATEGORY by PARENT_CATEGORY_CODE, named 'productCategorySelfList'.
     * @return The entity list of referrer property 'productCategorySelfList'. (NotNull: even if no loading, returns empty list)
     */
    public List<ProductCategory> getProductCategorySelfList() {
        if (_productCategorySelfList == null) { _productCategorySelfList = newReferrerList(); }
        return _productCategorySelfList;
    }

    /**
     * [set] (商品カテゴリ)PRODUCT_CATEGORY by PARENT_CATEGORY_CODE, named 'productCategorySelfList'.
     * @param productCategorySelfList The entity list of referrer property 'productCategorySelfList'. (NullAllowed)
     */
    public void setProductCategorySelfList(List<ProductCategory> productCategorySelfList) {
        _productCategorySelfList = productCategorySelfList;
    }

    protected <ELEMENT> List<ELEMENT> newReferrerList() { // overriding to import
        return new ArrayList<ELEMENT>();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    protected boolean doEquals(Object obj) {
        if (obj instanceof BsProductCategory) {
            BsProductCategory other = (BsProductCategory)obj;
            if (!xSV(_productCategoryCode, other._productCategoryCode)) { return false; }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int doHashCode(int initial) {
        int hs = initial;
        hs = xCH(hs, asTableDbName());
        hs = xCH(hs, _productCategoryCode);
        return hs;
    }

    @Override
    protected String doBuildStringWithRelation(String li) {
        StringBuilder sb = new StringBuilder();
        if (_productCategorySelf != null && _productCategorySelf.isPresent())
        { sb.append(li).append(xbRDS(_productCategorySelf, "productCategorySelf")); }
        if (_productList != null) { for (Product et : _productList)
        { if (et != null) { sb.append(li).append(xbRDS(et, "productList")); } } }
        if (_productCategorySelfList != null) { for (ProductCategory et : _productCategorySelfList)
        { if (et != null) { sb.append(li).append(xbRDS(et, "productCategorySelfList")); } } }
        return sb.toString();
    }
    protected <ET extends Entity> String xbRDS(org.dbflute.optional.OptionalEntity<ET> et, String name) { // buildRelationDisplayString()
        return et.get().buildDisplayString(name, true, true);
    }

    @Override
    protected String doBuildColumnString(String dm) {
        StringBuilder sb = new StringBuilder();
        sb.append(dm).append(xfND(_productCategoryCode));
        sb.append(dm).append(xfND(_productCategoryName));
        sb.append(dm).append(xfND(_parentCategoryCode));
        if (sb.length() > dm.length()) {
            sb.delete(0, dm.length());
        }
        sb.insert(0, "{").append("}");
        return sb.toString();
    }

    @Override
    protected String doBuildRelationString(String dm) {
        StringBuilder sb = new StringBuilder();
        if (_productCategorySelf != null && _productCategorySelf.isPresent())
        { sb.append(dm).append("productCategorySelf"); }
        if (_productList != null && !_productList.isEmpty())
        { sb.append(dm).append("productList"); }
        if (_productCategorySelfList != null && !_productCategorySelfList.isEmpty())
        { sb.append(dm).append("productCategorySelfList"); }
        if (sb.length() > dm.length()) {
            sb.delete(0, dm.length()).insert(0, "(").append(")");
        }
        return sb.toString();
    }

    @Override
    public ProductCategory clone() {
        return (ProductCategory)super.clone();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * [get] PRODUCT_CATEGORY_CODE: {PK, NotNull, CHAR(3)} <br>
     * 商品カテゴリコード
     * @return The value of the column 'PRODUCT_CATEGORY_CODE'. (basically NotNull if selected: for the constraint)
     */
    public String getProductCategoryCode() {
        checkSpecifiedProperty("productCategoryCode");
        return convertEmptyToNull(_productCategoryCode);
    }

    /**
     * [set] PRODUCT_CATEGORY_CODE: {PK, NotNull, CHAR(3)} <br>
     * 商品カテゴリコード
     * @param productCategoryCode The value of the column 'PRODUCT_CATEGORY_CODE'. (basically NotNull if update: for the constraint)
     */
    public void setProductCategoryCode(String productCategoryCode) {
        registerModifiedProperty("productCategoryCode");
        _productCategoryCode = productCategoryCode;
    }

    /**
     * [get] PRODUCT_CATEGORY_NAME: {NotNull, VARCHAR(50)} <br>
     * 商品カテゴリ名称
     * @return The value of the column 'PRODUCT_CATEGORY_NAME'. (basically NotNull if selected: for the constraint)
     */
    public String getProductCategoryName() {
        checkSpecifiedProperty("productCategoryName");
        return convertEmptyToNull(_productCategoryName);
    }

    /**
     * [set] PRODUCT_CATEGORY_NAME: {NotNull, VARCHAR(50)} <br>
     * 商品カテゴリ名称
     * @param productCategoryName The value of the column 'PRODUCT_CATEGORY_NAME'. (basically NotNull if update: for the constraint)
     */
    public void setProductCategoryName(String productCategoryName) {
        registerModifiedProperty("productCategoryName");
        _productCategoryName = productCategoryName;
    }

    /**
     * [get] (親カテゴリコード)PARENT_CATEGORY_CODE: {IX, CHAR(3), FK to product_category} <br>
     * 最上位の場合はデータなし。
     * @return The value of the column 'PARENT_CATEGORY_CODE'. (NullAllowed even if selected: for no constraint)
     */
    public String getParentCategoryCode() {
        checkSpecifiedProperty("parentCategoryCode");
        return convertEmptyToNull(_parentCategoryCode);
    }

    /**
     * [set] (親カテゴリコード)PARENT_CATEGORY_CODE: {IX, CHAR(3), FK to product_category} <br>
     * 最上位の場合はデータなし。
     * @param parentCategoryCode The value of the column 'PARENT_CATEGORY_CODE'. (NullAllowed: null update allowed for no constraint)
     */
    public void setParentCategoryCode(String parentCategoryCode) {
        registerModifiedProperty("parentCategoryCode");
        _parentCategoryCode = parentCategoryCode;
    }
}
