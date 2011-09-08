usage : 

complie a single file
=====================================
java -jar -b js_project_base -i input_file -o output_file

compile your whole project
=====================================
java -jar -b js_project_base -o output_path -i input_path -iext=_src.js -oext=js --disable-optimizations


complie css file
=====================================
java -jar -t -b js_project_base -i input_file -o output_file



Js Compressor build-in macro:

$include('model_relative_path'); // path is based on -b option when compress
$include('http://also.support.httpfile');


TODO List:
1. merger css @import grammar
2. code images in css to base64