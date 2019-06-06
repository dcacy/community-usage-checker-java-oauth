<!doctype html>

<html lang="en">
<head>
    <title>Community Usage Checker</title>
    <meta charset="utf-8">
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/carbon-components/css/carbon-components.min.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>css/styles.css">
    <link rel="shortcut icon" href="<%=request.getContextPath()%>/images/favicon.png">
</head>

<body>

	<!-- error message display -->
	<div class="errorMessage" id="error"></div>
    <div id="introText">
        Welcome to the IBM Connections Cloud Community Usage Checker!
        This application will list the communities you can access, and show you some 
        details about those communities.  It uses OAuth, so clicking the Login button below
        will attempt to log you in to IBM Connections Cloud. If that succeeds, you'll be prompted
        to grant access to this application, after which it will show you your communities.
        <p>Questions? Suggestions? Drop me a line at
        <a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;&#100;&#99;&#97;&#99;&#121;&#64;&#117;&#115;&#46;&#105;&#98;&#109;&#46;&#99;&#111;&#109;'>&#100;&#99;&#97;&#99;&#121;&#64;&#117;&#115;&#46;&#105;&#98;&#109;&#46;&#99;&#111;&#109;</a>
    </div>
	<div id="buttonWrapper" class="buttonWrapper">
    <a class="someButton" href="<%=request.getContextPath()%>/api?action=login">Log In</a>
  	</div>
	
	
	<!-- <script type="text/javascript" src="<%=request.getContextPath()%>/js/index.js"></script>-->
    
</body>
</html>
     