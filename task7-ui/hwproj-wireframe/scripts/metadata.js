define(function(){
    return {
        pageGroups: [{"id":"81b61290-8e0a-6d2e-a801-ee6e8350a238","name":"Default group","pages":[{"id":"c48ddad4-9163-a1a6-4f25-6e86629d08bf","name":"MainPageNotLogged"},{"id":"57ee8d0c-5846-2ba4-0011-7b872d94918a","name":"StudentMainPage"},{"id":"3ac6e69a-39ee-2545-f1c6-95efa2cf69ef","name":"StudentCoursePage"},{"id":"6846ff21-9e82-1ff4-af10-64a2b2a779a6","name":"StudentTaskPage"},{"id":"7ddfd71d-2cf0-0a1e-87fd-853c76603821","name":"TeacherMainPage"},{"id":"6d13bba2-6189-588c-bbd6-eb75b5569175","name":"TeacherCoursePage"},{"id":"9afdcd5a-71ab-ef1f-edd2-a786f59765fd","name":"TeacherStudDialogue"}]}],
        downloadLink: "//services.ninjamock.com/html/htmlExport/download?shareCode=SGFHH&projectName=hwproj",
        startupPageId: 0,

        forEachPage: function(func, thisArg){
        	for (var i = 0, l = this.pageGroups.length; i < l; ++i){
                var group = this.pageGroups[i];
                for (var j = 0, k = group.pages.length; j < k; ++j){
                    var page = group.pages[j];
                    if (func.call(thisArg, page) === false){
                    	return;
                    }
                }
            }
        },
        findPageById: function(pageId){
        	var result;
        	this.forEachPage(function(page){
        		if (page.id === pageId){
        			result = page;
        			return false;
        		}
        	});
        	return result;
        }
    }
});
