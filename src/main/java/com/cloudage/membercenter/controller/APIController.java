package com.cloudage.membercenter.controller;

import java.io.File;
import java.util.List;

import javax.crypto.AEADBadTagException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;

import com.cloudage.membercenter.entity.Article;
import com.cloudage.membercenter.entity.Comment;
import com.cloudage.membercenter.entity.User;
import com.cloudage.membercenter.service.IArticleService;
import com.cloudage.membercenter.service.ICommentService;
import com.cloudage.membercenter.service.IUserService;


@RestController
@RequestMapping("/api")
public class APIController {
	@Autowired
	IUserService userService;

	@Autowired
	IArticleService articleService;
	
	@Autowired
	ICommentService commentService;


	@RequestMapping(value = "/hello", method=RequestMethod.GET)
	public @ResponseBody String hello(){
		return "HELLO WORLD";
	}

	//	@RequestParam(name = "account") String account;
	//	

	//以下是注册模块的方法
	@RequestMapping(value = "/register", method=RequestMethod.POST)
	public  User register(

			@RequestParam String account,
			@RequestParam String passwordHash,
			@RequestParam  String email,
			@RequestParam  String name,
			MultipartFile avatar,
			HttpServletRequest request){

		User user = new User();
		user.setAccount(account);
		user.setPasswordHash(passwordHash);
		user.setEmail(email);
		user.setName(name);

		//判断图片是否为空
		if(avatar!=null){
			try {
				String realPath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");
				//------------
				FileUtils.copyInputStreamToFile(avatar.getInputStream(),new File(realPath,account+".png"));
				user.setAvatar("upload/"+account+".png");

			} catch (Exception e) {
				e.printStackTrace();
			}

		}


		return userService.save(user);

	}


	//登陆方法
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public User login(
			@RequestParam String account,
			@RequestParam String passwordHash,
			HttpServletRequest request){

		User user = userService.findByAccount(account); //找到用户

		//判断用户是否为空并且密码是否正确
		if(user!=null && user.getPasswordHash().equals(passwordHash)){
			HttpSession session = request.getSession(true);
			session.setAttribute("uid", user.getId());
			return user;
		}else{

			return null;
		}

	}

	//返回当前用户
	@RequestMapping(value="/me", method = RequestMethod.GET)
	public User getCurrentUser(HttpServletRequest request){
		HttpSession session = request.getSession(true);
		Integer uid = (Integer) session.getAttribute("uid");
		return userService.findById(uid);
		//		Object object = request.getSession().getAttribute("user");
		//		if(object instanceof User){
		//			//判断object是否为USER的实例，如果是则返回true，否则返回false
		//			return (User)object;
		//		}else{
		//			return null;
		//		}		
	}

	//忘记密码，通过邮箱修改密码
	@RequestMapping(value="/repassword",method = RequestMethod.POST)
	public boolean repassword(
			@RequestParam String email,
			@RequestParam String passwordHash,
			HttpServletRequest request){
		User user = userService.findByEmail(email);
		if (user == null){
			return false;
		}else{
			user.setPasswordHash(passwordHash);
			userService.save(user);
			return true;
		}
	}

	//重设密码
	@RequestMapping(value="/passwordrecover",method = RequestMethod.POST)
	public boolean ressetPassword(
			@RequestParam String email,
			@RequestParam String passwordHash){
		User user = userService.findByEmail(email);
		if (user == null){
			return false;
		}else{
			user.setPasswordHash(passwordHash);
			userService.save(user);
			return true;
		}
	}


	@RequestMapping(value="/articles/{userId}")
	public List<Article> getArticlesByUserID(@PathVariable Integer userId){
		return articleService.findAllByAuthorId(userId);
	}

	//编写文章
	@RequestMapping(value="/article",method=RequestMethod.POST)
	public Article addArticle(
			@RequestParam String title,
			@RequestParam String text,
			HttpServletRequest request){
		User currentUser = getCurrentUser(request);
		Article article = new Article();
		article.setAuthor(currentUser);
		article.setTitle(title);
		article.setText(text);
		return articleService.save(article);
	}
	
	//编写服务器Feeds文章网页显示
	@RequestMapping(value="/feeds/{page}")
	public Page<Article> getFeeds(@PathVariable int page){
		return articleService.getFeeds(page);
	}

	@RequestMapping("/feeds")
	public Page<Article> getFeeds(){
		return getFeeds(0);
	}
	
	//--------------
	//发表评论分页网页
	@RequestMapping(value="/article/{article_id}/comments/{page}")
	public Page<Comment> setCommentsOfArticle(
			@PathVariable int article_id,
			@PathVariable int page){
		return commentService.findCommentsOfArticle(article_id, page);
	}
	
	@RequestMapping(value="/article/{article_id}/comments")
	public Page<Comment> setCommentsOfArticle(
			@PathVariable int article_id){
		return commentService.findCommentsOfArticle(article_id, 0);
	}
	
	//发表评论----
	@RequestMapping(value="/article/{article_id}/comments",method = RequestMethod.POST)
	public Comment postComment(
			@PathVariable int article_id,
			@RequestParam String text,
			HttpServletRequest request){
		User me = getCurrentUser(request);
		Article article = articleService.findOne(article_id);
		Comment comment = new Comment();
		comment.setAuthor(me);          //评论人
		comment.setArticle(article);    //评论文章
		comment.setText(text);          //评论内容
		return commentService.save(comment);
	}

}
