This is an e-commerce project hosted on AWS. To get started run the below commands from cloudshell
Note: Please check the cost before running this project. A rough estimate is given in docs/costEstimate.

```
git clone https://github.com/EVikramss/ECOM.git
chmod -R +x ECOM
cd ECOM
nohup ./setupBuildEnv.sh &
tail -f nohup.out
```

This project is completely automated using Jenkins build for CI/CD. Architecture for the application & build systems is shown below.

![ECOM](docs/ECOM.jpg)

![ECOM](docs/BuildBox.jpg)
