
<!DOCTYPE html>
<html>
<head>
	<title>Community Usage Checker</title>
	<link type="text/css" rel="stylesheet" href="//cdn.datatables.net/1.10.13/css/jquery.dataTables.min.css">
	<link type="text/css" rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
	<link type="text/css" rel="stylesheet" href="https://unpkg.com/carbon-components/css/carbon-components.min.css">
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css"/>
	<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/css/hamster.css"/>
   <link rel="shortcut icon" href="<%=request.getContextPath()%>/images/favicon.png">
</head>

<body>
	<div class="title">Connections Cloud Community Usage Checker</div>
	<div id="hamster_loader_wrapper" class="loader_wrapper" style="display:flex;">
		<svg class="loader_img" xmlns="http://www.w3.org/2000/svg" id="Layer_1" data-name="Layer 1" xmlns:xlink="http://www.w3.org/1999/xlink"
			viewBox="0 0 100 100">
			<defs>
				<symbol id="hamster" data-name="hamster" viewBox="0 0 80 38">
					<g id="hamster-3" data-name="hamster">
						<path id="frontleg" d="M53 25l5 5" class="legs cls-1"></path>
						<path id="backLeg" d="M10 33l5-5" class="legs cls-2"></path>
						<path class="hamsterFill" id="body" d="M64 4c-5-4-13-4-16-3l-6 4c-2 1-11 5-25 1-1-1-11-1-15 8a13 13 0 0 0 4 16c25 17 49 4 58-10s2-15 0-16z"></path>
						<circle class="hamsterFill" id="tail" cx="2" cy="14" r="2"></circle>
						<circle class="hamsterFill" id="ear" cx="45" cy="3" r="3"></circle>
						<path id="eye" d="M55 5a2 2 0 0 1 3 3 2 2 0 0 1-3-3z" class="cls-3"></path>
					</g>
				</symbol>
			</defs>
			<path id="wheel" d="M85 15a49 49 0 0 0-70 70 49 49 0 1 0 70-70zm-64 6a41 41 0 0 1 58 0l5 6-34 21-35-20zM9 50a41 41 0 0 1 4-18l35 19v40A41 41 0 0 1 9 50zm70 29a41 41 0 0 1-27 12V51l34-20a41 41 0 0 1-7 48z"
				class="cls-4"></path>
			<use id="whisky" width="80" height="37.58" xlink:href="#hamster"></use>
		</svg>
	</div>
	<div id="error"></div>
		
	<div id="bodyWrapper" style="display:none;">
		<div id="userInfo"></div>
		<div id="moreCommunities">
			Note: 50 communities shown. To see up to 500 communities, click <button onclick="getAllCommunities(true);">here</button>.
		</div>
		<div id="communitiesTableWrapper" style="width:60%;">
			<div class="communityNote">Note: <span class="asterisks">***</span> = restricted community</div>
			<div class="tableHeader">Communities</div>
			<div id="communitiesLoadingDiv" class="loadingDiv" style="text-align:center;"></div>	  		
	   	<table class="table table-bordered display" id="communitiesTable"></table>
		</div>
	
		<div id="communityDetailsWrapper" style="display:block;">
					<div id="hamster_details_loader_wrapper" class="loader_wrapper" style="display:none;">
						<svg class="loader_img" xmlns="http://www.w3.org/2000/svg" id="Layer_1" data-name="Layer 1" xmlns:xlink="http://www.w3.org/1999/xlink"
							viewBox="0 0 100 100">
							<defs>
								<symbol id="hamster" data-name="hamster" viewBox="0 0 80 38">
									<g id="hamster-3" data-name="hamster">
										<path id="frontleg" d="M53 25l5 5" class="legs cls-1"></path>
										<path id="backLeg" d="M10 33l5-5" class="legs cls-2"></path>
										<path class="hamsterFill" id="body" d="M64 4c-5-4-13-4-16-3l-6 4c-2 1-11 5-25 1-1-1-11-1-15 8a13 13 0 0 0 4 16c25 17 49 4 58-10s2-15 0-16z"></path>
										<circle class="hamsterFill" id="tail" cx="2" cy="14" r="2"></circle>
										<circle class="hamsterFill" id="ear" cx="45" cy="3" r="3"></circle>
										<path id="eye" d="M55 5a2 2 0 0 1 3 3 2 2 0 0 1-3-3z" class="cls-3"></path>
									</g>
								</symbol>
							</defs>
							<path id="wheel" d="M85 15a49 49 0 0 0-70 70 49 49 0 1 0 70-70zm-64 6a41 41 0 0 1 58 0l5 6-34 21-35-20zM9 50a41 41 0 0 1 4-18l35 19v40A41 41 0 0 1 9 50zm70 29a41 41 0 0 1-27 12V51l34-20a41 41 0 0 1-7 48z"
								class="cls-4"></path>
							<use id="whisky" width="80" height="37.58" xlink:href="#hamster"></use>
						</svg>
					</div>
			<div class="communityDetailsInnerWrapper" style="display:none;">
				<div class="tabWrapper">
					<div class="tableHeader">Details</div>
					<div>Community Name: <span id="communityName"></span></div>
					<div>Community ID: <span id="communityId"></span></div>
				  	<div id="communityDetails"></div>
		  		</div>
		  	</div>
		</div>
  </div>
  <script>
  var contextPath = "<%=request.getContextPath()%>"; // for use in scripts
  </script>

	<script type="text/javascript" src="//code.jquery.com/jquery-3.1.1.min.js"></script>
	<script type="text/javascript" src="//code.jquery.com/ui/1.12.1/jquery-ui.min.js"></script>
	<script type="text/javascript" src="//cdn.datatables.net/1.10.13/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/date.format.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/script.js"></script>
</body>
</html>
