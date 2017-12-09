:: ein komentar
echo Hallo was geht?

rmdir /S /Q out

mkdir out
mkdir out\image

javac ./src/sample/*.java -d ./out

copy src\image out\image
copy src\sample\*.fxml out\sample

