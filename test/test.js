
var test = {obj:1,}
/** test mod include **/
$include('mod.js','gbk');
$include('charset.js');
$include('http://test.com/123.js');

define(function(require, exports, module) {
  exports.sayHello = function() {
    alert('Hello from module: ' + module.id);
  };
});