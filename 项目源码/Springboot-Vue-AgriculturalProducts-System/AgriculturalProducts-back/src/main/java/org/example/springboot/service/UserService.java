package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.mapper.*;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.MenusUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private MenuMapper menuMapper;
    @Value("${user.defaultPassword}")
    private String DEFAULT_PWD;



    @Resource
    private AddressMapper addressMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private ProductMapper productMapper;
@Resource
private ReviewMapper reviewMapper;
@Resource
private CartMapper cartMapper;



@Resource
private FavoriteMapper favoriteMapper;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    @Resource
    private StockInMapper stockInMapper;


    public User getByEmail(String email){
        LambdaQueryWrapper<User> studentLambdaQueryWrapper = new LambdaQueryWrapper<User>().eq(User::getEmail, email);
        return userMapper.selectOne(studentLambdaQueryWrapper);
    }

    public Result<?> login(User user){
        User compare = getByUsername(user.getUsername());
        if(compare==null)return Result.error("-1","用户不存在");
        if(compare.getStatus().equals(AccountStatus.DISABLED.getValue()))return Result.error("-1","账号被禁用");
        if(compare != null && bCryptPasswordEncoder.matches(user.getPassword(), compare.getPassword())){
            List<Menu> roleMenuList = menuMapper.selectList(null);
            String token = JwtTokenUtils.genToken(String.valueOf(compare.getId()), compare.getPassword());
            compare.setMenuList(MenusUtils.allocMenus(roleMenuList,compare.getRole()));
            compare.setToken(token);
            return Result.success(compare);
        }
        return Result.error("-1","用户名或密码错误",null);

    }
    public List<User> getUserByRole(String role) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getRole, role);
        return userMapper.selectList(queryWrapper);
    }
    public int createUser(User user) {
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername())) != null) {
            return -1;
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail())) != null) {
            return -2;
        }
        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRole(StringUtils.isNotBlank(user.getRole()) ? user.getRole() : "USER");
        user.setStatus(AccountStatus.ENABLED.getValue());
        return userMapper.insert(user);
    }
    public Integer deleteUserById(int id) {

        deleteUserRelations(id);
        if(!checkStockRelation(id)){
            return -2;
        }
        if(!checkUserProducts(id)){
            return -1;
        }
        return userMapper.deleteById(id);
    }
    public boolean updateUser(Long id, User user) {
        user.setId(id);
        return userMapper.updateById(user) > 0;
    }
    public User getByUsername(String  username){
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,username));
    }
    public boolean forgetPassword(String email, String newPassword) {
        User oldUser = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        if (oldUser != null) {
            oldUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
            return userMapper.updateById(oldUser) > 0;
        }
        return false;
    }

    public boolean updatePassword(int id, UserPasswordUpdate userPasswordUpdate) {
        User oldUser = userMapper.selectById(id);
        if (oldUser != null &&bCryptPasswordEncoder.matches(userPasswordUpdate.getOldPassword(),oldUser.getPassword())) {
            oldUser.setPassword(bCryptPasswordEncoder.encode(userPasswordUpdate.getNewPassword()));
            return userMapper.updateById(oldUser) > 0;
        }

        return false;
    }
    public Integer deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            deleteUserRelations(id);
            if(!checkStockRelation(id)){
                return -2;
            };
            if(!checkUserProducts(id)){
                return -1;
            }
        }
        return userMapper.deleteByIds(ids) ;
    }

    public List<User> getAllUsers() {
        return userMapper.selectList(new QueryWrapper<>());
    }
    public User getUserById(int id) {
        User user = userMapper.selectById(id);
        if (user != null && user.getStatus().equals(AccountStatus.ENABLED.getValue())) {
            return user;
        }
        return null;
    }
    public Page<User> getUsersByPage(String username, String name, String role, String status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }

        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(User::getName, name);
        }
        if (StringUtils.isNotBlank(role)) {
            queryWrapper.eq(User::getRole, role);
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(User::getStatus, status);
        }
        Page<User> page = new Page<>(currentPage, size);
        return userMapper.selectPage(page, queryWrapper);

    }
    private boolean checkUserProducts(int id){
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, id));
        for (Product product : products) {
            if(product!=null){
                return false;

            }
        }
        return true;
    }

    private void deleteUserRelations(int id){
   
                List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>().eq(Address::getUserId, id));
        for (Address address : addresses) {
            if(address!=null){
                addressMapper.deleteById(address);
            }
        }

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getUserId, id));
        for (Order order : orders) {
            if(order!=null){
                orderMapper.deleteById(order);
            }
        }

        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>().eq(Review::getUserId, id));
        for (Review review : reviews) {
            if(review!=null){
                reviewMapper.deleteById(review);
            }
        }
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, id));
        for (Cart cart : carts) {
            if(cart!=null){
                cartMapper.deleteById(cart);
            }
        }

        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, id));
        for (Favorite favorite : favorites) {
            if(favorite!=null){
                favoriteMapper.deleteById(favorite);
            }
        }

    }
  private   Boolean checkStockRelation(int id){
      List<StockIn> stockIns = stockInMapper.selectList(new LambdaQueryWrapper<StockIn>().eq(StockIn::getOperatorId, id));
      for (StockIn stockIn : stockIns) {
          if(stockIn!=null){
                return false;
          }
      }
      return true;
    }

    private void deleteProductRelations(Long productId){
        orderMapper.delete(new LambdaQueryWrapper<Order>().eq(Order::getProductId,productId));
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>().eq(Favorite::getProductId,productId));
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getProductId,productId));
        reviewMapper.delete(new LambdaQueryWrapper<Review>().eq(Review::getProductId,productId));

    }
}
