$VERSION="2.1.0"
$IMAGE = "server:5000/raidschedulerbot"

$IMAGE_WITH_VERSION = -join($IMAGE, ":", $VERSION)
$IMAGE_LATEST = -join($IMAGE, ":latest")

mvn clean install -DskipTests

docker buildx create --use --name buildx_instance

#build x86 version
docker buildx build . --load `
--platform linux/amd64 `
--build-arg VERSION=$VERSION `
--tag $IMAGE_WITH_VERSION `
--tag $IMAGE_LATEST

docker buildx stop buildx_instance

docker push $IMAGE_WITH_VERSION
docker push $IMAGE_LATEST

git tag v$VERSION
git push origin tag v$VERSION