'use strict';

/* App Module */
var myModule = angular.module('bankapp', 
                ['highcharts-ng','TestData', 'angular-loading-bar', 'ngRoute', 'ngSanitize', 
                'productServices', 'ngCookies'])

myModule.config(function ($routeProvider) {
    $routeProvider
    .when('/builds/', {  templateUrl: 'partials/bankapp/overview.html',
                                        controller: OverviewCtrl })
    .when('/builds/:owner/:project/builds', {  templateUrl: 'partials/bankapp/build.html',
                                        controller: BuildsCtrl })
	.when('/builds/:owner/:project/testsuites/:buildnumber', {  templateUrl: 'partials/bankapp/testsuite.html',
                                        controller: TestSuitesCtrl })
	.when('/builds/:owner/:project/testcases/:testsuiteid', {  templateUrl: 'partials/bankapp/testcase.html',
                                        controller: TestCasesCtrl })
    .otherwise({ redirectTo: '/builds' });
})
