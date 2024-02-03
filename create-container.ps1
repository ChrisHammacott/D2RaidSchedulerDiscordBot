$VERSION="2.0.0"

mvn clean install -DskipTests

docker buildx create --use --name buildx_instance

#build x86 version
docker buildx build -t raidschedulerbot:$VERSION --build-arg BOT_VERSION=$VERSION --load .
#build arm version
docker buildx build --platform linux/arm64 -t raidschedulerbot-arm:$VERSION --build-arg BOT_VERSION=$VERSION --load .

docker buildx stop buildx_instance

#save image to deploy to server
docker save --output raidschedulerbot-arm-$VERSION.tar raidschedulerbot-arm:$VERSION