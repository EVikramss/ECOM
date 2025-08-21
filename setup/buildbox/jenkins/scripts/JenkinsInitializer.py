import requests
import json
import time

class JenkinsInitializer:

    def __init__(self): 
        # session object
        self.session = requests.Session()
        
    def process(self):
        try:
            # get login page
            response = self.session.get('http://localhost:8080/login?from=%2F')
        
            initPass = self.readPassword(response)
            
            # login and get crumb
            post_data = {'from': '/', 'j_username': 'admin', 'j_password' : initPass}
            response = self.session.post('http://localhost:8080/j_spring_security_check', data = post_data)
            crumb = self.extractText('data-crumb-value="', '"', response.text)
            
            # install suggested plugins
            suggestedPlugins = self.getSuggestedPlugins()
            statusCode = self.installPlugins(crumb, suggestedPlugins)
        
            if statusCode == 200:
                self.waitForPluginInstallationComplete()
                #self.createUser()
                self.configureInstance()
            else:
                print(response.text)
                
        except Exception as e:
            print('Unable to setup jenkins')
            print(e)
            
    def readPassword(self, response):
        loginPageResponse = response.text
        passwordFile = self.extractText('<code>', '<', loginPageResponse)
        file = open(passwordFile)
        password = file.read()
        file.close()
        #return password[0:-1]
        return password.strip()
        
    def extractText(self, startText, endToken, responseData):
        startIndex = responseData.find(startText) + len(startText)
        endIndex = responseData.find(endToken, startIndex)
        output = responseData[startIndex:endIndex]
        return output
        
    def getSuggestedPlugins(self):
        # on login success, fetch suggested plugin list
        response = self.session.get('http://localhost:8080/setupWizard/platformPluginList')
        data = response.json()['data']
        suggestedPlugins = []
        
        for category in data:
            pluginArr = category['plugins']
            for plugin in pluginArr:
                pluginName = plugin['name']
                if 'suggested' in plugin:
                    isSuggested = plugin['suggested']
                    if isSuggested:
                        suggestedPlugins.append(pluginName)
                        
        return suggestedPlugins
        
    def installPlugins(self, crumb, suggestedPlugins):
        # install suggested plugins
        post_data = json.dumps({"dynamicLoad":True,"plugins":suggestedPlugins,"Jenkins-Crumb":crumb})
        response = self.session.post('http://localhost:8080/pluginManager/installPlugins', data = post_data, headers = {'Content-Type':'application/json', 'jenkins-crumb' : crumb})
                        
        return response.status_code
        
    def waitForPluginInstallationComplete(self):
        
        installationComplete = False
        
        while not(installationComplete):
            print('Installing plugins ...')
            response = self.session.get('http://localhost:8080/updateCenter/installStatus')
            data = response.json()['data']
            state = data['state']
            if state == 'CREATE_ADMIN_USER':
                installationComplete = True
                print('Plugins installed')
            else:
                time.sleep(10)
                
    def createUser(self):
        response = self.session.get('http://localhost:8080/setupWizard/setupWizardFirstUser')
        crumb = self.extractText('data-crumb-value="', '"', response.text)

        # Form data
        post_data = {"username": "admin", "password1": "password", "password2": "password", "fullname": "admin", "email": "test@test.com", "Jenkins-Crumb": crumb,"json": '{"username":"admin","password1":"password","$redact":["password1","password2"],"password2":"password","fullname":"admin","email":"test@test.com","Jenkins-Crumb":"' + crumb + '"}',"core:apply": "", "Submit": "Save"}

        response = self.session.post('http://localhost:8080/setupWizard/createAdminUser', data = post_data, headers = {'Content-Type':'application/json', 'jenkins-crumb' : crumb})
        
        print('User created')
        
    def configureInstance(self):
        response = self.session.get('http://localhost:8080/setupWizard/setupWizardConfigureInstance')
        crumb = self.extractText('data-crumb-value="', '"', response.text)
        
        post_data = json.dumps({"rootUrl":"http://localhost:8080/","Jenkins-Crumb":crumb})
        response = self.session.post('http://localhost:8080/setupWizard/configureInstance', data = post_data, headers = {'Content-Type':'application/json', 'jenkins-crumb' : crumb})
        
        post_data = json.dumps({"Jenkins-Crumb":crumb})
        response = self.session.post('http://localhost:8080/setupWizard/completeInstall', data = post_data, headers = {'Content-Type':'application/json', 'jenkins-crumb' : crumb})
        
        print('Install completed')

initializer = JenkinsInitializer()
initializer.process()