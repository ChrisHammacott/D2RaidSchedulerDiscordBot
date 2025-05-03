#! /bin/bash

version="2.1.0"
image="server:5000/raidschedulerbot"

image_version="${image}:${version}"
image_latest="${image}:latest"

mvn clean install -DskipTests

docker buildx create --use --name buildx_instance

#build x86 version
docker buildx build . --load \
--platform linux/amd64 \
--build-arg VERSION=${version} \
--tag ${image_version} \
--tag ${image_latest}

docker buildx stop buildx_instance

docker push ${image_version}
docker push ${image_latest}

git tag v${version}
git push origin tag v${version}