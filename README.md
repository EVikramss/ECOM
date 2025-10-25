This is an e-commerce project hosted on AWS. To get started run the below commands from cloudshell - 



git clone https://github.com/EVikramss/ECOM.git

chmod -R +x ECOM

cd ECOM

nohup ./setupBuildEnv.sh &

tail -f nohup.out


This project is completely automated using jenkins build for CI/CD. Architecture for the build and application is shown below.
