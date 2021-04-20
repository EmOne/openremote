DEPLOYMENT_NAME=${1:?usage $0 DEPLOYMENT_NAME}
AWS_ECR_REPO=$(docker run --rm -v ~/.aws:/root/.aws -v /var/run/docker.sock:/var/run/docker.sock --entrypoint aws openremote/openremote-cli ecr describe-repositories --repository-names $DEPLOYMENT --query 'repositories[0].repositoryUri' --profile openremote-cli | xargs echo)
DOCKER_PASSWORD=$(docker run --rm -v ~/.aws:/root/.aws -v /var/run/docker.sock:/var/run/docker.sock --entrypoint aws openremote/openremote-cli ecr get-login-password --profile openremote-cli)
docker login --username AWS --password $DOCKER_PASSWORD $AWS_ECR_REPO
docker run --rm -v ~/.aws:/root/.aws -v $(pwd):/$DEPLOYMENT_NAME openremote/openremote-cli map -f $DEPLOYMENT_NAME
echo $AWS_ECR_REPO
docker-compose down
docker images --filter "reference=$AWS_ECR_REPO" -q | xargs docker rmi
sudo rm -rf deployment
docker run --rm -v ~/.aws:/root/.aws -v $(pwd):/$DEPLOYMENT_NAME openremote/openremote-cli map -a download -f $DEPLOYMENT_NAME/mapdata.mbtiles -v -t
docker run --rm -v ~/.aws:/root/.aws -v $(pwd):/$DEPLOYMENT_NAME openremote/openremote-cli map -a download -f $DEPLOYMENT_NAME/deployment.tar.gz -v -t
sudo chown -R ubuntu:ubuntu .
sudo tar -xf deployment.tar.gz
sudo mv mapdata.mbtiles deployment/map/
docker-compose up -d
docker images --filter "reference=openremote/openremote-cli" -q | xargs docker rmi
