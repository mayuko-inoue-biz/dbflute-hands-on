package org.docksidestage.handson.dbflute.bsbhv.loader;

import java.util.List;

import org.dbflute.bhv.*;
import org.dbflute.bhv.referrer.*;
import org.docksidestage.handson.dbflute.exbhv.*;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.dbflute.cbean.*;

/**
 * The referrer loader of PRODUCT as TABLE.
 * @author DBFlute(AutoGenerator)
 */
public class LoaderOfProduct {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected List<Product> _selectedList;
    protected BehaviorSelector _selector;
    protected ProductBhv _myBhv; // lazy-loaded

    // ===================================================================================
    //                                                                   Ready for Loading
    //                                                                   =================
    public LoaderOfProduct ready(List<Product> selectedList, BehaviorSelector selector)
    { _selectedList = selectedList; _selector = selector; return this; }

    protected ProductBhv myBhv()
    { if (_myBhv != null) { return _myBhv; } else { _myBhv = _selector.select(ProductBhv.class); return _myBhv; } }

    // ===================================================================================
    //                                                                       Load Referrer
    //                                                                       =============
    protected List<Purchase> _referrerPurchase;

    /**
     * Load referrer of purchaseList by the set-upper of referrer. <br>
     * (購入)PURCHASE by PRODUCT_ID, named 'purchaseList'.
     * <pre>
     * <span style="color: #0000C0">productBhv</span>.<span style="color: #994747">load</span>(<span style="color: #553000">productList</span>, <span style="color: #553000">productLoader</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">productLoader</span>.<span style="color: #CC4747">loadPurchase</span>(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *         <span style="color: #553000">purchaseCB</span>.setupSelect...
     *         <span style="color: #553000">purchaseCB</span>.query().set...
     *         <span style="color: #553000">purchaseCB</span>.query().addOrderBy...
     *     }); <span style="color: #3F7E5E">// you can load nested referrer from here</span>
     *     <span style="color: #3F7E5E">//}).withNestedReferrer(<span style="color: #553000">purchaseLoader</span> -&gt; {</span>
     *     <span style="color: #3F7E5E">//    purchaseLoader.load...</span>
     *     <span style="color: #3F7E5E">//});</span>
     * });
     * for (Product product : <span style="color: #553000">productList</span>) {
     *     ... = product.<span style="color: #CC4747">getPurchaseList()</span>;
     * }
     * </pre>
     * About internal policy, the value of primary key (and others too) is treated as case-insensitive. <br>
     * The condition-bean, which the set-upper provides, has settings before callback as follows:
     * <pre>
     * cb.query().setProductId_InScope(pkList);
     * cb.query().addOrderBy_ProductId_Asc();
     * </pre>
     * @param refCBLambda The callback to set up referrer condition-bean for loading referrer. (NotNull)
     * @return The callback interface which you can load nested referrer by calling withNestedReferrer(). (NotNull)
     */
    public NestedReferrerLoaderGateway<LoaderOfPurchase> loadPurchase(ReferrerConditionSetupper<PurchaseCB> refCBLambda) {
        myBhv().loadPurchase(_selectedList, refCBLambda).withNestedReferrer(refLs -> _referrerPurchase = refLs);
        return hd -> hd.handle(new LoaderOfPurchase().ready(_referrerPurchase, _selector));
    }

    // ===================================================================================
    //                                                                    Pull out Foreign
    //                                                                    ================
    protected LoaderOfProductCategory _foreignProductCategoryLoader;
    public LoaderOfProductCategory pulloutProductCategory() {
        if (_foreignProductCategoryLoader == null)
        { _foreignProductCategoryLoader = new LoaderOfProductCategory().ready(myBhv().pulloutProductCategory(_selectedList), _selector); }
        return _foreignProductCategoryLoader;
    }

    protected LoaderOfProductStatus _foreignProductStatusLoader;
    public LoaderOfProductStatus pulloutProductStatus() {
        if (_foreignProductStatusLoader == null)
        { _foreignProductStatusLoader = new LoaderOfProductStatus().ready(myBhv().pulloutProductStatus(_selectedList), _selector); }
        return _foreignProductStatusLoader;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<Product> getSelectedList() { return _selectedList; }
    public BehaviorSelector getSelector() { return _selector; }
}
