
var test = {obj:1,}
/** test mod include **/
$include('mod.js','gbk');
$include('charset.js');

define(function(require, exports, module) {
  exports.sayHello = function() {
    alert('Hello from module: ' + module.id);
  };
});