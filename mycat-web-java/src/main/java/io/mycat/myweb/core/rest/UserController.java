package io.mycat.myweb.core.rest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import io.mycat.dao.bean.RestResult;
import io.mycat.myweb.core.domain.User;
import io.mycat.myweb.core.service.TokenService;
import io.mycat.myweb.core.service.UserService;
import io.mycat.myweb.core.util.TokenUtil;

@RestController
public class UserController {
	@Autowired
	UserService userService;
	@Autowired
	TokenService tokenService;

	// 登录
	@PostMapping("/login")
	@ResponseBody
	public String login(@RequestBody User user) {
		RestResult rest = new RestResult();
		Optional<User> userForBase = userService.findByUserName(user.getName());
		User dbuser = userForBase.orElse(null);
		if (dbuser == null) {
			rest.data = Json.createValue("登录失败,用户不存在");
			rest.retCode = 410;
			return rest.toJSonString();
		} else {
			if (!dbuser.getPassword().equals(user.getPassword())) {
				rest.data = Json.createValue("登录失败,密码错误");
				rest.retCode = 411;
				return rest.toJSonString();
			} else {
				Map<String, Object> tmpMap = setConditionMap(userForBase);
				String token = tokenService.getToken(dbuser);
				Map<String, Object> data = new HashMap<>();
				data.put("token", token);
				data.put("user", tmpMap);
				rest.data = Json.createObjectBuilder(data).build();
				return rest.toJSonString();
			}
		}
	}

	@RequestMapping(value = "/getMessage", method = RequestMethod.GET)
	public String getMessage() {

		// 取出token中带的用户id 进行操作
		System.out.println(TokenUtil.getTokenUserId());

		return "您已通过验证";
	}
	
	// 此行一下为临时操作工具

		/**
		 * 将一个类查询方式加入map（属性值为int型时，0时不加入， 属性值为String型或Long时为null和“”不加入）
		 *
		 */
		private Map<String, Object> setConditionMap(Object obj) {
			Map<String, Object> map = new HashMap<String, Object>();
			if (obj == null) {
				return null;
			}
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				Class<?> type = field.getType();
				Object valueByFieldName = getValueByFieldName(fieldName, obj);
				if (Date.class==type) {
					valueByFieldName = new SimpleDateFormat("yyyy-MM-dd").format(valueByFieldName);
				}
				if (valueByFieldName != null)
					map.put(fieldName, valueByFieldName);
			}

			return map;

		}
		
		

		/**
		 * 根据属性名获取该类此属性的值
		 * 
		 * @param fieldName
		 * @param object
		 * @return
		 */
		private Object getValueByFieldName(String fieldName, Object object) {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);
			try {
				Method method = object.getClass().getMethod(getter, new Class[] {});
				Object value = method.invoke(object, new Object[] {});
				return value;
			} catch (Exception e) {
				return null;
			}

		}
}