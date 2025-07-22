# quarkus-rds-persistence



## Preparando o ambiente

Primeiro, vamos começar preparando o ambiente, instalando o Python 3, o gerenciador de pacotes pip e bibliotecas AWS (sdk boto).

```
sudo dnf install -y python3 python3-virtualenv python3-pip boto3 botocore
```

A seguir, vamos criar e ativar um ambiente virtual isolado (.venv), garantindo que as instalações futuras não afetem o sistema.

```
python3 -m venv .venv
```
```
source .venv/bin/activate
```

Depois, atualizamos as ferramentas de empacotamento e instalamos o Ansible nesse ambiente.

```
pip install --upgrade pip setuptools wheel
```
```
pip install ansible
```

Agora, precisamos confirmar a instalação checando a versão do Ansible.

```
ansible --version
```
 
Em nosso exemplo, foi instalado o Ansible na versão 2.18.6.

![Ansible version](./images/ansible/01%20-%20Ansible%20version.png)

Vamos instalar localmente a collection **amazon.aws**. Com ela podemos ter acesso a todos os módulos, plugins e roles da AWS para uso nos nossos playbooks Ansible.

```
ansible-galaxy collection install amazon.aws
```

Para o próximo passo, iremos editar o arquivo de credenciais de acesso à AWS. Os atributos aws_access_key_id e aws_secret_access_key podem ser obtidos no serviço IAM na console de gerenciamento AWS.

```
code ~/.aws/credentials
```

O formato do arquivo deve ser algo parecido com: <br>
**[default] <br>
aws_access_key_id     = <key_id> <br>
aws_secret_access_key = <access_key>**

Vamos verificar se as credenciais informadas funcionam e se há conectividade à API da AWS.

```
ansible localhost -m amazon.aws.aws_caller_info -c local
```

Se o comando retornar sucesso, isso significa que o Ansible conseguiu se autenticar na AWS e está acessível a partir do host que rodou o comando.

![ AWS connection with Ansible OK](./images/ansible/02%20-%20AWS%20connection%20with%20Ansible%20OK.png)


## Provisionando recursos na AWS usando playbook Ansible

Esse laboratório já possui as playbooks pré-configuradas para uso com todos os recursos necessários para provisionamento. Essas playbooks econtram-se disponíveis em: 

Para começar precisamos definir uma vault ansible para guardar a senha do nosso banco de dados MySQL.

>> **Vaults** são mecanismos para criptografar e proteger dados sensíveis (como senhas, chaves e tokens) que ficam em arquivos YAML. Com o Vault você pode cifrar e decifrar esses arquivos através de comandos e utilizá-los em playbooks sem expor segredos no repositório, garantindo que apenas quem conhece a senha ou possui o arquivo de senha consiga acessar o conteúdo protegido.

Execute o comando abaixo para fazer a criação da vault.

```
ansible-vault create vault.yml
```

Nesse momento é solicitada a criação e confirmação de uma senha para acesso ao vault. No nosso exemplo, usamos a senha **redhat** Após informar, é criado um arquivo vazio chamado vault.yml.

Aqui, vamos incluir o seguinte trecho *rds_password: "<db_password>"*. No nosso exemplo, definimos a senha **dbrdspass**. Essa será a senha do usuário admin do banco.

![Creating RDS password on vault](./images/ansible/03%20-%20Creating%20RDS%20password%20on%20vault.png)

Após salvar o arquivo, ele será criptografado automaticamente.

![Encrypted vault](./images/ansible/04%20-%20Encrypted%20vault.png)

Uma outra forma de fazer isso seria criar o arquivo vault.yml manualmente, incluir o conteúdo e em seguida executar o comando abaixo. Nesse momento será solicitada a criação e confirmação de uma senha para o vault, assim como na forma anterior.

```
ansible-vault encrypt vault.yml
```

Caso queira descriptografar essa vault, substituindo a versão cifrada pela versão em texto puro, é só executar o comando abaixo. A senha criada para o vault será solicitada.

```
ansible-vault decrypt vault.yml
```

Agora vamos executar a playbook. A execução desse comando deve demorar alguns minutos pois todos os recursos devem ser provisionados na AWS.

```
ansible-playbook -i localhost, aws-create-infra.yaml --ask-vault-pass
```

![Create infra playbook](./images/ansible/05%20-%20Create%20infra%20playbook.png)

Ao final da execução, um relatório é gerado no terminal para informar o status da execução dessa playbook.

![Play recap creation playbook](./images/ansible/06%20-%20Play%20recap%20creation%20playbook.png)

Confira na console de gerenciamento da AWS, os recursos que foram criados na região sa-east-1.

* VPC e componentes de rede:
![Provisioned VPC](./images/aws/01%20-%20Provisioned%20VPC.png)

* Instância EC2 Bastion:
![Provisioned EC2 Bastion](./images/aws/02%20-%20Provisioned%20EC2%20Bastion.png)

* Security Groups:
![Provisioned Security Groups](./images/aws/03%20-%20Provisioned%20Security%20Groups.png)

* Banco de dados RDS MySQL:
![Provisioned RDS](./images/aws/04%20-%20Provisioned%20RDS.png)

Finalizamos essa seção com sucesso. Agora, vamos fazer o deploy da nossa aplicação no Red Hat OpenShift e as devidas configurações para estabelecer o acesso ao banco de dados.


## Deploy da aplicação no OpenShift


## Creating VPC Peering

1. Criar o VPC Peering (Região Solicita → Região Aceita)
No Console AWS, mude o Region (canto superior direito) para us-east-2.

Acesse o serviço VPC.

No menu lateral, clique em Peering Connections.

Clique em Create Peering Connection.

Preencha:

Name tag: por exemplo, openshift-to-rds

VPC (Requester): selecione a VPC do seu OpenShift em us-east-2

Account: My account

Region (Peer): selecione sa-east-1

VPC (Accepter): selecione a VPC onde está o RDS em sa-east-1

Clique em Create Peering Connection.

2. Aceitar o Peering na Região de Destino (sa-east-1)
Ainda no Console AWS, mude o Region para sa-east-1.

Vá novamente em VPC → Peering Connections.

Você verá o peering criado em estado Pending Acceptance.

Selecione-o e clique em Actions → Accept Request.

Confirme em Accept.

3. Atualizar as Route Tables
3.1. Na VPC do OpenShift (us-east-2)
Volte a Region = us-east-2.

Em VPC → Route Tables, localize a Route Table usada pelos nós/pods do seu OpenShift.

Com ela selecionada, na aba Routes, clique em Edit routes → Add route:

Destination: CIDR da VPC do RDS (ex.: 10.2.0.0/16)

Target: selecione o seu Peering Connection (pcx-...)

Salve.

3.2. Na VPC do RDS (sa-east-1)
Mude para Region = sa-east-1.

Em VPC → Route Tables, escolha a Route Table associada às subnets do RDS.

Em Routes → Edit routes → Add route:

Destination: CIDR da VPC do OpenShift (ex.: 10.1.0.0/16)

Target: mesmo Peering Connection (pcx-...)

Salve.

4. Ajustar o Security Group do RDS
Ainda em Region = sa-east-1:

Vá em EC2 → Security Groups.

Encontre o SG associado ao seu RDS.

Na aba Inbound rules, clique em Edit inbound rules → Add rule:

Type: MySQL/Aurora (TCP 3306)

Source: digite o CIDR da VPC do OpenShift (ex.: 10.1.0.0/16) ou selecione o Security Group dos nós do OpenShift.

Salve as regras.


## Validando o acesso ao banco de dados

Agora, vamos logar na nossa intância EC2 que faz o papel de bastion via SSH usando uma chave pem.

```
ssh -i "aws-bastion-key.pem" ec2-user@ec2-18-230-88-94.sa-east-1.compute.amazonaws.com
```

Uma vez dentro da instância do bastion, vamos atualizar todos os pacotes instalados no sistema para a versão mais recente disponível nos repositórios, e na sequência instalar o mysql via linha de comando. Por fim, vamos nos conectar à instância RDS onde temos nosso banco de dados. 

> *O atributo host a ser informado deve ser o definido para a instância do RDS.*

```
sudo yum update -y
```
```
sudo yum install -y mysql
```
```
mysql --host=music-api-db.cvtpccljwu07.sa-east-1.rds.amazonaws.com --port=3306 --user=admin --password musicdb
```

O resultado deve ser algo como:

*Welcome to the MariaDB monitor.  Commands end with ; or \g.*
*Your MySQL connection id is 27*
*Server version: 8.0.41 Source distribution*

*Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.*

*Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.*

*MySQL [musicdb]>*

Vamos criar o usuário dbuser que será necessário na configuração da aplicação que vai persistir as informações no banco de dados.

```
CREATE USER 'dbuser'@'%' IDENTIFIED BY 'dbpass';
```
```
GRANT 
    SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES
ON musicdb.* 
TO 'dbuser'@'%';
```
```
FLUSH PRIVILEGES;
```

## Testando o funcionamento

Primeiro, vamos fazer as requisições do tipo POST, para salvar as músicas no banco de dados.

curl -X POST https://musicplayer-app.apps.cluster-qdq57.qdq57.sandbox1229.opentlc.com/api/songs \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Stairway to Heaven",
        "artist": {
          "name": "Led Zeppelin"
        }
      }'

curl -X POST https://musicplayer-app.apps.cluster-qdq57.qdq57.sandbox1229.opentlc.com/api/songs \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Smoke on the Water",
        "artist": {
          "name": "Deep Purple"
        }
      }'

curl -X POST https://musicplayer-app.apps.cluster-qdq57.qdq57.sandbox1229.opentlc.com/api/songs \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Back in Black",
        "artist": {
          "name": "AC/DC"
        }
      }'


A seguir, para testar se as informações foram salvas no banco de dados corretamente, é possível usar os seguintes comandos:
```
select * from songs;
```
```
select * from artists;
```
```
select s.id, s.title, a.name from songs s inner join artists a on s.artist_id = a.id;
```

## Destroy

ansible-playbook -i localhost, aws-destroy-infra.yaml --ask-vault-pass