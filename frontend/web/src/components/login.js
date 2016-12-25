import React, { Component } from 'react';
import config from "../config";

export default class Login extends Component {
	constructor (props) {
		super(props);

		this.state = {};
	}

	onFormSubmit(event) {
		event.preventDefault();
		var email = this.refs.email.value;
	    var password = this.refs.password.value;
		var userInfo = {email: email, password: password};
		$.ajax({
		    type: "POST",
		    url: config.apiBaseUrl+"users/auth",
		    contentType: "application/json",
		    dataType: "json",
		    data: JSON.stringify(userInfo),
		    success: function(result) {
		    	localStorage.setItem("bearer_token", result.token);
		    	this.props.setLogin(true);
		    }.bind(this),
		    error: function() {
		    	$(".login-error").text("Login error");
		    	console.log("Login error");
		    }
		});
	}

	fbLogin() {
		console.log("fb login");
		var self = this;
		FB.login(function(response) {
			  if (response.status === 'connected') {
				self.sendSocialLogin();
			  } else if (response.status === 'not_authorized') {
			  	$(".login-error").text("Login error");
			    console.log("No facebook login authorization");
			  } else {
			  	$(".login-error").text("Login error");
			    console.log("Unknown fb login error");
			  }
		}, {scope: 'public_profile,email'});
	}

	sendSocialLogin() {
		FB.api('/me', { locale: 'en_US', fields: 'name, email' }, function(response) {
		    var userInfo = {email: response.email, name: response.name, fbtoken: response.id};
			$.ajax({
			    type: "POST",
			    url: config.apiBaseUrl+"users/facebook",
			    contentType: "application/json",
			    dataType: "json",
			    data: JSON.stringify(userInfo),
			    success: function(result) {
			    	localStorage.setItem("bearer_token", result.token);
			    	this.props.setLogin(true);
			    }.bind(this),
			    error: function() {
			    	$(".login-error").text("Login error");
			    	console.log("Login error");
			    }
			});
		}.bind(this));
	}


	render() {
		var fbLoginBtn = require("../imgs/fb-login-btn.png");
		return (
		  <div className="container login">
		      <div className="row">
		        <div className="col l6 offset-l3 s12 center hint">Default email: test@test.com, pw: test123</div>
		      </div>
		      <div className="row">
		        <form className="col s12" onSubmit={this.onFormSubmit.bind(this)}>
		          <div className="row">
		            <div className="input-field col l6 offset-l3 s12">
		              <input name="email" id="email" type="email" ref="email" className="validate" defaultValue="test@test.com"/>
		              <label htmlFor="email">Email</label>
		            </div>
		          </div>
		          <div className="row">
		            <div className="input-field col l6 offset-l3 s12">
		              <input name="password" id="password" type="password" ref="password" className="validate" defaultValue="test123"/>
		              <label htmlFor="password">Password</label>
		            </div>
		          </div>
		          <div className="row center">
		            <button className="btn waves-effect waves-light" type="submit" name="action">OK
		              <i className="material-icons right">send</i>
		            </button>
		          </div>
		        </form>
		      </div>
		      <div className="row center">
		      	<img className="fb-btn" onClick={this.fbLogin.bind(this)} src={fbLoginBtn}/>
		      </div>
		      <div className="row">
		        <div className="col l6 offset-l3 s12 center login-error"></div>
		      </div>
		    </div>
		);
	}
}
