package com.in28minutes.rest.webservices.restfulwebservices.user;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.in28minutes.rest.webservices.restfulwebservices.jpa.PostRepository;
import com.in28minutes.rest.webservices.restfulwebservices.jpa.UserRepository;

import jakarta.validation.Valid;

@RestController
public class UserJpaResource {
	 private UserRepository repository;
	private PostRepository postRepository;
	 public UserJpaResource(UserRepository repository,PostRepository postRepository){
		 this.repository = repository;  
		 this.postRepository = postRepository;
		 
	 }
	 
	 //GET /users (Retrieve all users)
	 @GetMapping("/jpa/users")
     public List<User> retrieveAllUsers(){
		 
    	 return repository.findAll();
     }
	 // http://localhost:8080/users
	 //EntityModel
	 //WebMvcLinkBuilder
	 
	 //GET /users/{id} (Retrieving specific user with a given id.)
	 @GetMapping("/jpa/users/{id}")
     public EntityModel<User> retrieveUser(@PathVariable int id){
    	 Optional<User> user = repository.findById(id);
    	 if(user.isEmpty()) {
    		 throw new UserNotFoundException("id:"+id);
    	 }
    	 EntityModel<User> entityModel = EntityModel.of(user.get()); 
    	 WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).retrieveAllUsers());
    	 entityModel.add(link.withRel("all-users"));
		return entityModel;
     }
	 // DELETE /users{id} (Deleting a user)
	 @DeleteMapping("/jpa/users/{id}")
     public void deleteUser(@PathVariable int id){
    	 repository.deleteById(id);
     }
	 // GET /jpa/users/{id}/posts (Retrieving post from a user)
	 @GetMapping("/jpa/users/{id}/posts")
     public List<Post> retrievePostsForUser(@PathVariable int id){
    	 Optional<User> user = repository.findById(id);
    	 if(user.isEmpty()) {
    		 throw new UserNotFoundException("id:"+id);
    	 }
    	 return user.get().getPosts();
     }
	 //GET /jpa/users/{id}/posts/{post_id} (Retrieving specific post for a user)
	 @GetMapping("/jpa/users/{id}/posts/{post_id}")
     public Post retrievePostforPostId(@PathVariable int id,@PathVariable int post_id,@Valid Post post){
		
		 Optional<User> user = repository.findById(id);
    	 if(user.isEmpty()) {
    		 throw new UserNotFoundException("id:"+id);
    	 }
    	 Predicate<? super Post> predicate = post1->post1.getId()==post_id;
		return user.get().getPosts().stream().filter(predicate).findFirst().get();
     }
	 // DELETE /jpa/users/{id}/posts (Deleting all posts for a specified user)
	 @DeleteMapping("/jpa/users/{id}/posts")
     public void deletePostsForUser(@PathVariable int id){
    	 Optional<User> user = repository.findById(id);
    	 if(user.isEmpty()) {
    		 throw new UserNotFoundException("id:"+id);
    	 }
    	  user.get().getPosts().clear();
     }
	//DELETE /jpa/users/{id}/posts/{post_id} (Deleting specific post for a user)
	 @DeleteMapping("/jpa/users/{id}/posts/{post_id}")
     public void deletePostforPostId(@PathVariable int id,@PathVariable int post_id,@Valid Post post){
		
		 Optional<User> user = repository.findById(id);
    	 if(user.isEmpty()) {
    		 throw new UserNotFoundException("id:"+id);
    	 }
    	 Predicate<? super Post> predicate = post1->post1.getId()==post_id;
		 user.get().getPosts().removeIf(predicate);
     }
	 
	 // POST /users (To create a new user)
	 @PostMapping("/jpa/users")
	 public ResponseEntity<User> createUser(@Valid @RequestBody User user){
		 User savedUser = repository.save(user);
		 // /users/4   /users/{id}   user.getId()
 		 URI location=ServletUriComponentsBuilder.fromCurrentRequest()
 				      .path("/{id}")
 				       .buildAndExpand(savedUser.getId()).toUri();
		return ResponseEntity.created(location).build();
	 }
	 // POST /users/{id}/posts create new post for a specified user
	 @PostMapping("/jpa/users/{id}/posts")
     public ResponseEntity<Object> createPostForUser(@PathVariable int id,@Valid @RequestBody Post post){
    	 Optional<User> user = repository.findById(id);
    	 if(user.isEmpty()) {
    		 throw new UserNotFoundException("id:"+id);
    	 }
    	 
    	  post.setUser(user.get());
    	  Post savedPost = postRepository.save(post);
    	 URI location=ServletUriComponentsBuilder.fromCurrentRequest()
			      .path("/{id}")
			       .buildAndExpand(savedPost.getId()).toUri();
	     return ResponseEntity.created(location).build();
     }

}
