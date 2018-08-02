package com.fulu.game.core.service.impl;


import com.fulu.game.common.enums.RedisKeyEnum;
import com.fulu.game.common.exception.ProductException;
import com.fulu.game.common.exception.ServiceErrorException;
import com.fulu.game.core.dao.ICommonDao;
import com.fulu.game.core.dao.ProductDao;
import com.fulu.game.core.entity.*;
import com.fulu.game.core.entity.vo.*;
import com.fulu.game.core.search.component.ProductSearchComponent;
import com.fulu.game.core.search.domain.ProductShowCaseDoc;
import com.fulu.game.core.service.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.xiaoleilu.hutool.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;


@Service
@Slf4j
public class ProductServiceImpl extends AbsCommonService<Product, Integer> implements ProductService {

    @Autowired
    private ProductDao productDao;
    @Autowired
    private UserTechAuthService userTechAuthService;
    @Autowired
    private RedisOpenServiceImpl redisOpenService;
    @Autowired
    private UserInfoAuthService userInfoAuthService;
    @Autowired
    private TechTagService techTagService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SalesModeService salesModeService;
    @Autowired
    private ProductSearchComponent productSearchComponent;
    @Autowired
    private ProductTopService productTopService;

    @Override
    public ICommonDao<Product, Integer> getDao() {
        return productDao;
    }

    @Override
    public Product create(Integer techAuthId, BigDecimal price, Integer unitId) {
        User user = userService.getCurrentUser();
        log.info("创建接单方式:userId:{};techAuthId:{};price:{};unitId:{}", user.getId(), techAuthId, price, unitId);
        UserTechAuth userTechAuth = userTechAuthService.findById(techAuthId);
        if (userTechAuth == null) {
            throw new ServiceErrorException("不能设置该技能接单!");
        }
        userService.isCurrentUser(userTechAuth.getUserId());
        //检查用户技能状态
        userTechAuthService.checkUserTechAuth(techAuthId);
        //检查用户认证的状态
        userService.checkUserInfoAuthStatus(user.getId());

        //查询销售方式的单位
        SalesMode salesMode = salesModeService.findById(unitId);
        if (salesMode == null) {
            throw new ServiceErrorException("单位不能为空!");
        }
        Category category = categoryService.findById(userTechAuth.getCategoryId());
        if (!salesMode.getCategoryId().equals(category.getId())) {
            throw new ServiceErrorException("接单方式单位不匹配!");
        }
        List<Product> products = findProductByUserAndSalesMode(user.getId(), userTechAuth.getId(), unitId);
        if (products.size() > 0) {
            throw new ServiceErrorException("不能设置同样单位的技能!");
        }
        Product product = new Product();
        product.setCategoryId(userTechAuth.getCategoryId());
        product.setGender(user.getGender());
        product.setCategoryIcon(category.getIcon());
        product.setProductName(userTechAuth.getCategoryName());
        product.setTechAuthId(userTechAuth.getId());
        product.setSalesModeId(salesMode.getId());
        product.setUnit(salesMode.getName());
        product.setSalesModeRank(salesMode.getRank() == null ? 0 : salesMode.getRank());
        product.setUserId(userTechAuth.getUserId());
        product.setPrice(price);
        product.setStatus(false);
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        product.setIsActivate(Boolean.TRUE);
        product.setDelFlag(Boolean.FALSE);
        create(product);
        return product;
    }


    @Override
    public Product update(Integer id, Integer techAuthId, BigDecimal price, Integer unitId) {
        User user = userService.getCurrentUser();
        log.info("修改接单方式:userId:{};techAuthId:{};price:{};unitId:{}", user.getId(), techAuthId, price, unitId);
        //检查用户技能状态
        userTechAuthService.checkUserTechAuth(techAuthId);
        if (redisOpenService.hasKey(RedisKeyEnum.PRODUCT_ENABLE_KEY.generateKey(id))) {
            throw new ServiceErrorException("在线技能不允许修改!");
        }
        Product product = findById(id);
        if (product == null) {
            throw new ProductException(ProductException.ExceptionCode.PRODUCT_REVIEW_ING);
        }
        userService.isCurrentUser(product.getUserId());
        //检查用户认证的状态
        userService.checkUserInfoAuthStatus(product.getUserId());
        if (techAuthId != null) {
            if (!product.getTechAuthId().equals(techAuthId)) {
                List<Product> products = findProductByUserAndSalesMode(product.getUserId(), techAuthId, unitId);
                if (products.size() > 0) {
                    throw new ServiceErrorException("不能设置同样单位的技能!");
                }
            }
            UserTechAuth userTechAuth = userTechAuthService.findById(techAuthId);
            userService.isCurrentUser(userTechAuth.getUserId());
            Category category = categoryService.findById(userTechAuth.getCategoryId());
            product.setCategoryId(userTechAuth.getCategoryId());
            product.setProductName(userTechAuth.getCategoryName());
            product.setTechAuthId(userTechAuth.getId());
            product.setCategoryIcon(category.getIcon());
        }
        if (price != null) {
            product.setPrice(price);
        }
        if (unitId != null) {
            if (!product.getSalesModeId().equals(unitId)) {
                List<Product> products = findProductByUserAndSalesMode(product.getUserId(), techAuthId, unitId);
                if (products.size() > 0) {
                    throw new ServiceErrorException("不能设置同样单位的技能!");
                }
            }
            SalesMode salesMode = salesModeService.findById(unitId);
            product.setSalesModeId(salesMode.getId());
            product.setUnit(salesMode.getName());
            product.setSalesModeRank(salesMode.getRank());
        }
        product.setUpdateTime(new Date());
        update(product);
        return product;
    }


    @Override
    public void updateByCategory(Category category) {
        if (category.getName() == null && category.getIcon() == null) {
            return;
        }
        productDao.updateByCategory(category);
    }

    /**
     * 激活或者取消激活商品
     *
     * @param product
     * @param status
     * @return
     */
    @Override
    public Product enable(Product product, boolean status) {
        User user = userService.getCurrentUser();
        log.info("激活或者取消激活商品:userId:{};product:{};status:{};", user.getId(), product, status);
        //检查用户认证的状态
        userService.checkUserInfoAuthStatus(user.getId());
        if (redisOpenService.hasKey(RedisKeyEnum.PRODUCT_ENABLE_KEY.generateKey(product.getId()))) {
            throw new ServiceErrorException("在线技能不允许修改!");
        }
        product.setStatus(status);
        update(product);
        if (!status) {
            saveProductIndex(product, Boolean.FALSE);
        }
        return product;
    }


    @Override
    public void techEnable(int techId, boolean status) {
        log.info("激活或取消该技能下所有商品:techId:{};status:{};", techId, status);
        User user = userService.getCurrentUser();
        userService.checkUserInfoAuthStatus(user.getId());
        List<Product> products = findByTechId(techId);
        for (Product product : products) {
            enable(product, status);
        }
    }


    @Override
    public List<Product> findByTechId(int techId) {
        ProductVO param = new ProductVO();
        param.setTechAuthId(techId);
        return productDao.findByParameter(param);
    }

    /**
     * 查找激活的商品
     *
     * @param userId
     * @return
     */
    public List<Product> findEnabledProductByUser(int userId) {
        ProductVO productVO = new ProductVO();
        productVO.setStatus(true);
        productVO.setUserId(userId);
        return productDao.findByParameter(productVO);
    }

    /**
     * 查询用户是否设置了同样单位的商品
     *
     * @param userId
     * @param salesModeId
     * @return
     */
    public List<Product> findProductByUserAndSalesMode(int userId, int techAuthId, int salesModeId) {
        ProductVO productVO = new ProductVO();
        productVO.setTechAuthId(techAuthId);
        productVO.setUserId(userId);
        productVO.setSalesModeId(salesModeId);
        return productDao.findByParameter(productVO);
    }

    /**
     * 根据技能查询所有的商品
     *
     * @param techAuthId
     * @return
     */
    public List<Product> findProductByTech(int techAuthId) {
        ProductVO productVO = new ProductVO();
        productVO.setTechAuthId(techAuthId);
        return productDao.findByParameter(productVO);
    }


    public List<TechAuthProductVO> techAuthProductList(int userId) {
        List<UserTechAuth> userTechAuths = userTechAuthService.findByUserId(userId);
        List<TechAuthProductVO> resultList = new ArrayList<>();
        for (UserTechAuth userTechAuth : userTechAuths) {
            TechAuthProductVO techAuthProductVO = new TechAuthProductVO();
            BeanUtil.copyProperties(userTechAuth, techAuthProductVO);
            List<Product> list = findProductByTech(userTechAuth.getId());
            techAuthProductVO.setProductList(list);
            resultList.add(techAuthProductVO);
        }
        return resultList;
    }


    /**
     * 批量更新所有的商品索引
     */
    public void bathUpdateProductIndex() {
        log.info("批量更新所有商品索引");
        List<User> userList = userService.findAllServeUser();
        for (User user : userList) {
            updateUserProductIndex(user.getId(), Boolean.TRUE);
        }
    }

    /**
     * 恢复商品删除状态
     *
     * @param productId
     */
    public void recoverProductActivate(int productId) {
        log.info("恢复商品删除状态:productId:{}", productId);
        productDao.recoverProductActivate(productId);
    }


    public void recoverProductActivateByTechAuthId(Integer techAuthId) {
        log.info("通过techAuthId恢复商品删除状态:techAuthId:{}", techAuthId);
        productDao.recoverProductActivateByTechAuthId(techAuthId);
    }

    @Override
    public int updateProductSalesModel(SalesMode salesMode) {
        return productDao.updateProductSalesModel(salesMode);
    }

    /**
     * 开始接单业务
     */
    @Override
    public void startOrderReceiving(Float hour) {
        User user = userService.getCurrentUser();
        log.info("用户开始接单:userId:{};hour:{};", user.getId(), hour);
        userService.isCurrentUser(user.getId());
        //检查用户认证的状态
        userService.checkUserInfoAuthStatus(user.getId());

        Long expire = (long) (hour * 3600);
        List<Product> products = findEnabledProductByUser(user.getId());
        if (products.isEmpty()) {
            throw new ServiceErrorException("请选择技能后再点击开始接单!");
        }
        redisOpenService.hset(RedisKeyEnum.USER_ORDER_RECEIVE_TIME_KEY.generateKey(user.getId()), "HOUR", hour, expire);
        redisOpenService.hset(RedisKeyEnum.USER_ORDER_RECEIVE_TIME_KEY.generateKey(user.getId()), "START_TIME", new Date().getTime(), expire);
        for (Product product : products) {
            ProductVO productVO = new ProductVO();
            BeanUtil.copyProperties(product, productVO);
            productVO.setHour(hour);
            productVO.setStartTime(new Date());
            try {
                log.info("开始接单设置:userId:{};product:{};beginOrderDate:{};orderHour:{};", product.getUserId(), product, System.currentTimeMillis(), hour);
                redisOpenService.hset(RedisKeyEnum.PRODUCT_ENABLE_KEY.generateKey(product.getId()), BeanUtil.beanToMap(productVO), expire);
            } catch (Exception e) {
                log.error("开始接单设置失败", e);
                throw new ServiceErrorException("开始接单操作失败!");
            }
        }
        //添加商品到首页
        updateUserProductIndex(user.getId(), Boolean.TRUE);
    }


    /**
     * 手动停止接单
     */
    @Override
    public void stopOrderReceiving() {
        User user = userService.getCurrentUser();
        log.info("用户停止接单:userId:{};", user.getId());
        //检查用户认证的状态
        userService.checkUserInfoAuthStatus(user.getId());
        List<Product> products = findEnabledProductByUser(user.getId());
        redisOpenService.delete(RedisKeyEnum.USER_ORDER_RECEIVE_TIME_KEY.generateKey(user.getId()));
        for (Product product : products) {
            try {
                log.info("停止接单设置:userId:{};product:{};endOrderDate:{};", product.getUserId(), product, System.currentTimeMillis());
                redisOpenService.delete(RedisKeyEnum.PRODUCT_ENABLE_KEY.generateKey(product.getId()));
            } catch (Exception e) {
                log.error("停止接单设置失败", e);
                throw new ServiceErrorException("开始接单操作失败!");
            }
        }
        //修改首页商品的状态
        updateUserProductIndex(user.getId(), Boolean.FALSE);
    }


    /**
     * 查询商品详情页
     *
     * @param productId
     * @return
     */
    @Override
    public ProductDetailsVO findDetailsByProductId(Integer productId) {
        Product product = findById(productId);
        if (product == null) {
            throw new ProductException(ProductException.ExceptionCode.PRODUCT_NOT_EXIST);
        }
        //查询用户信息
        UserInfoVO userInfo = userInfoAuthService.findUserCardByUserId(product.getUserId(), true, true, true, false);
        //查询技能标签
        List<String> techTags = new ArrayList<>();
        List<TechTag> techTagList = techTagService.findByTechAuthId(product.getTechAuthId());
        for (TechTag techTag : techTagList) {
            techTags.add(techTag.getName());
        }
        List<ProductVO> productVOList = findOtherProductVO(product.getUserId(), productId);
        //查询用户认证的技能
        UserTechAuth userTechAuth = userTechAuthService.findById(product.getTechAuthId());
        //查询完成订单数
        int orderCount = orderService.allOrderCount(userInfo.getUserId());
        //查询用户段位信息
        ProductDetailsVO productDetailsVO = ProductDetailsVO.builder()
                .categoryId(product.getCategoryId())
                .id(product.getId())
                .onLine(isProductStartOrderReceivingStatus(product.getId()))
                .description(userTechAuth.getDescription())
                .productName(product.getProductName())
                .categoryIcon(product.getCategoryIcon())
                .price(product.getPrice())
                .unit(product.getUnit())
                .techAuthId(product.getTechAuthId())
                .userInfo(userInfo)
                .orderCount(orderCount)
                .techTags(techTags)
                .otherProduct(productVOList)
                .build();

        UserTechInfo userTechInfo = userTechAuthService.findDanInfo(product.getTechAuthId());
        if (userTechInfo != null) {
            productDetailsVO.setDan(userTechInfo.getValue());
        }
        return productDetailsVO;
    }


    @Override
    public SimpleProductVO findSimpleProductByProductId(Integer productId) {
        Product product = findById(productId);
        if (product == null) {
            throw new ProductException(ProductException.ExceptionCode.PRODUCT_NOT_EXIST);
        }
        UserInfoVO userInfo = userInfoAuthService.findUserCardByUserId(product.getUserId(), false, false, false, false);
        SimpleProductVO simpleProductVO = new SimpleProductVO();
        BeanUtil.copyProperties(product, simpleProductVO);
        simpleProductVO.setUserInfo(userInfo);
        return simpleProductVO;
    }


    /**
     * 查找用户其他的商品
     *
     * @param userId
     * @param productId
     * @return
     */
    public List<ProductVO> findOtherProductVO(Integer userId, Integer productId) {
        List<Product> products = findByUserId(userId);
        List<ProductVO> productVOS = new ArrayList<>();
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                continue;
            }
            ProductVO productVO = new ProductVO();
            BeanUtil.copyProperties(product, productVO);
            List<String> techTags = new ArrayList<>();
            List<TechTag> techTagList = techTagService.findByTechAuthId(productVO.getTechAuthId());
            for (TechTag techTag : techTagList) {
                techTags.add(techTag.getName());
            }
            if (isUserStartOrderReceivingStatus(userId)) {
                if (isProductStartOrderReceivingStatus(productVO.getId())) {
                    productVO.setOnLine(true);
                    productVO.setTechTags(techTags);
                    productVOS.add(productVO);
                }
            } else {
                productVO.setOnLine(isProductStartOrderReceivingStatus(productVO.getId()));
                productVO.setTechTags(techTags);
                productVOS.add(productVO);
            }
        }
        return productVOS;
    }


    /**
     * 为用户所有商品添加索引
     *
     * @param userId
     */
    @Override
    public void updateUserProductIndex(Integer userId, Boolean needUpdateTime) {
        List<Product> products = findByUserId(userId);
        List<Integer> rightfulProductIds = new ArrayList<>();
        for (Product product : products) {
            //是否需要更新接单时间,如果不需要就获取索引里面的更新时间
            if (needUpdateTime) {
                product.setCreateTime(new Date());
            } else {
                ProductShowCaseDoc productShowCaseDoc = productSearchComponent.searchById(product.getId(), ProductShowCaseDoc.class);
                if (productShowCaseDoc != null) {
                    product.setCreateTime(productShowCaseDoc.getCreateTime());
                }
            }
            rightfulProductIds.add(product.getId());
        }
        //删除掉之前用户的垃圾商品数据
        List<ProductShowCaseDoc> productShowCaseDocList = productSearchComponent.findByUser(userId);
        for (ProductShowCaseDoc pDoc : productShowCaseDocList) {
            if (!rightfulProductIds.contains(pDoc.getId())) {
                productSearchComponent.deleteIndex(pDoc.getId());
            }
        }
        batchUpdateProductIndex(products);
    }


    /**
     * 搜索商品
     *
     * @param pageNum
     * @param pageSize
     * @param nickName
     * @return
     */

    @Override
    public PageInfo searchContent(int pageNum, int pageSize, String nickName) {
        try {
            Page<ProductShowCaseDoc> searchResult = productSearchComponent.findByNickName(pageNum, pageSize, nickName);
            return new PageInfo<>(searchResult);
        } catch (Exception e) {
            log.error("查询出错:查询内容:{};", nickName);
            log.error("查询出错:", e);
        }
        return new PageInfo();
    }

    /**
     * 商品首页和列表页
     *
     * @param categoryId
     * @param gender
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @Override
    public PageInfo<ProductShowCaseVO> findProductShowCase(Integer categoryId,
                                                           Integer gender,
                                                           Integer pageNum,
                                                           Integer pageSize,
                                                           String orderBy) {
        PageInfo<ProductShowCaseVO> pageInfo;
        try {
            long startTime = System.currentTimeMillis();
            Page<ProductShowCaseVO> searchResult = productSearchComponent.searchShowCaseDoc(categoryId,
                    gender, pageNum, pageSize, orderBy, ProductShowCaseVO.class);
            pageInfo = new PageInfo<>(searchResult);
            log.info("findProductShowCase查询es耗时{}", (float) (System.currentTimeMillis() - startTime) / 1000);
        } catch (Exception e) {
            log.error("ProductShowCase查询异常:", e);
            PageHelper.startPage(pageNum, pageSize, "create_time desc");
            long startTime = System.currentTimeMillis();
            List<ProductShowCaseVO> showCaseVOS = productDao.findProductShowCase(categoryId, gender);
            log.info("findProductShowCase查询db耗时{}", (float) (System.currentTimeMillis() - startTime) / 1000);
            for (ProductShowCaseVO showCaseVO : showCaseVOS) {
                UserInfoVO userInfoVO = userInfoAuthService.findUserCardByUserId(showCaseVO.getUserId(), Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
                showCaseVO.setNickName(userInfoVO.getNickName());
                showCaseVO.setGender(userInfoVO.getGender());
                showCaseVO.setMainPhoto(userInfoVO.getMainPhotoUrl());
                showCaseVO.setCity(userInfoVO.getCity());
                showCaseVO.setPersonTags(userInfoVO.getTags());
                UserTechInfo userTechInfo = userTechAuthService.findDanInfo(showCaseVO.getTechAuthId());
                if (userTechInfo != null) {
                    showCaseVO.setDan(userTechInfo.getValue());
                }
                showCaseVO.setOnLine(isProductStartOrderReceivingStatus(showCaseVO.getId()));
            }
            pageInfo = new PageInfo<>(showCaseVOS);
            log.info("findProductShowCase查询db然后for耗时{}", (float) (System.currentTimeMillis() - startTime) / 1000);
        }
        return pageInfo;
    }

    /**
     * 批量创建商品索引
     *
     * @param products (每个用户的所有商品集合)
     */
    private void batchUpdateProductIndex(List<Product> products) {
        if (products.isEmpty()) {
            return;
        }
        List<Product> showIndexProducts = getShowIndexProduct(products);
        for (Product product : products) {
            if (showIndexProducts.contains(product)) {
                saveProductIndex(product, Boolean.TRUE);
            } else {
                saveProductIndex(product, Boolean.FALSE);
            }
        }
    }

    /**
     * 查询那些可以在首页显示的商品
     *
     * @param products
     * @return
     */
    private List<Product> getShowIndexProduct(List<Product> products) {
        Map<Integer, List<Product>> categoryProductMap = new HashMap<>();
        for (Product product : products) {
            Integer categoryId = product.getCategoryId();
            if (categoryProductMap.containsKey(categoryId)) {
                categoryProductMap.get(categoryId).add(product);
            } else {
                categoryProductMap.put(categoryId, Lists.newArrayList(product));
            }
        }
        List<Product> showIndexProducts = new ArrayList<>();
        categoryProductMap.forEach((k, v) -> {
            List<Product> waitProducts = new ArrayList<>();
            v.forEach((p) -> {
                if (p.getStatus()) {
                    waitProducts.add(p);
                }
            });
            if (!waitProducts.isEmpty()) {
                waitProducts.sort((Product p1, Product p2) -> p1.getPrice().compareTo(p2.getPrice()));
                showIndexProducts.add(waitProducts.get(0));
            }
        });
        return showIndexProducts;
    }


    /**
     * 保存商品索引
     *
     * @param product
     * @param isIndexShow
     * @return
     */
    private ProductShowCaseDoc saveProductIndex(Product product, Boolean isIndexShow) {
        ProductShowCaseDoc productShowCaseDoc = new ProductShowCaseDoc();
        BeanUtil.copyProperties(product, productShowCaseDoc);
        UserInfoVO userInfoVO = userInfoAuthService.findUserCardByUserId(productShowCaseDoc.getUserId(), Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        //查询销量
        int userOrderCount = orderService.weekOrderCount(product.getUserId());
        productShowCaseDoc.setNickName(userInfoVO.getNickName());
        productShowCaseDoc.setGender(userInfoVO.getGender());
        productShowCaseDoc.setMainPhoto(userInfoVO.getMainPhotoUrl());
        productShowCaseDoc.setCity(userInfoVO.getCity());
        productShowCaseDoc.setIsIndexShow(isIndexShow);
        productShowCaseDoc.setPersonTags(userInfoVO.getTags());
        productShowCaseDoc.setOnLine(isProductStartOrderReceivingStatus(productShowCaseDoc.getId()));
        productShowCaseDoc.setOrderCount(userOrderCount);
        //查询段位信息
        UserTechInfo userTechInfo = userTechAuthService.findDanInfo(product.getTechAuthId());
        if (userTechInfo != null) {
            productShowCaseDoc.setDan(userTechInfo.getValue());
        }
        //查询置顶排序
        int topSort = productTopService.findTopSortByUserCategory(product.getUserId(), product.getCategoryId());
        productShowCaseDoc.setTopSort(topSort);

        log.info("插入索引:{}", productShowCaseDoc);
        Boolean result = productSearchComponent.saveProductIndex(productShowCaseDoc);
        if (!result) {
            log.error("插入索引失败:{}", productShowCaseDoc);
            //todo 如果插入索引结果失败如何做处理
        }
        return productShowCaseDoc;
    }


    /**
     * 查询用户全部的商品
     *
     * @param userId
     * @return
     */
    @Override
    public List<Product> findByUserId(Integer userId) {
        ProductVO productVO = new ProductVO();
        productVO.setUserId(userId);
        return productDao.findByParameter(productVO);
    }


    /**
     * 删除该技能下的所有商品
     */
    public void disabledProductByTech(Integer techAuthId) {
        log.info("删除技能下所有商品techAuthId:{}", techAuthId);
        List<Product> productList = findProductByTech(techAuthId);
        for (Product product : productList) {
            disabledProduct(product);
        }
    }


    /**
     * 删除该用户的所有商品
     */
    public void disabledProductByUser(Integer userId) {
        log.info("删除用户所有商品userId:{}", userId);
        redisOpenService.delete(RedisKeyEnum.USER_ORDER_RECEIVE_TIME_KEY.generateKey(userId));
        List<Product> productList = findByUserId(userId);
        for (Product product : productList) {
            disabledProduct(product);
        }
    }

    public int deleteById(Integer id) {
        return productDao.deleteById(id);
    }


    @Override
    public int disabledProduct(Product product) {
        redisOpenService.delete(RedisKeyEnum.PRODUCT_ENABLE_KEY.generateKey(product.getId()));
        productSearchComponent.deleteIndex(product.getId());
        log.info("删除商品product:{}", product);
        return productDao.disabledProductById(product.getId());
    }

    /**
     * 判断商品是否是开始接单状态
     *
     * @param productId
     * @return
     */
    @Override
    public Boolean isProductStartOrderReceivingStatus(Integer productId) {
        return redisOpenService.hasKey(RedisKeyEnum.PRODUCT_ENABLE_KEY.generateKey(productId));
    }


    @Override
    public Boolean isUserStartOrderReceivingStatus(Integer userId) {
        return redisOpenService.hasKey(RedisKeyEnum.USER_ORDER_RECEIVE_TIME_KEY.generateKey(userId));
    }


    public Map<String, Object> readOrderReceivingStatus() {
        User user = userService.getCurrentUser();
        //检查用户认证的状态
        userService.checkUserInfoAuthStatus(user.getId());
        return redisOpenService.hget(RedisKeyEnum.USER_ORDER_RECEIVE_TIME_KEY.generateKey(user.getId()));
    }

    /**
     * 查找用户其他的商品
     *
     * @param productId
     * @return
     */
    public List<ProductVO> findOthersByproductId(Integer productId) {
        Product mainProduct = productDao.findById(productId);
        ProductVO requestVO = new ProductVO();
        requestVO.setUserId(mainProduct.getUserId());
        requestVO.setCategoryId(mainProduct.getCategoryId());
        List<Product> products = productDao.findByParameter(requestVO);
        List<ProductVO> productVOS = new ArrayList<>();
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                continue;
            }
            ProductVO productVO = new ProductVO();
            BeanUtil.copyProperties(product, productVO);
            List<String> techTags = new ArrayList<>();
            List<TechTag> techTagList = techTagService.findByTechAuthId(productVO.getTechAuthId());
            for (TechTag techTag : techTagList) {
                techTags.add(techTag.getName());
            }
            productVO.setTechTags(techTags);
            productVOS.add(productVO);
        }
        return productVOS;
    }


}
