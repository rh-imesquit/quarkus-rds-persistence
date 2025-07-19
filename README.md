# quarkus-rds-persistence




sudo dnf install -y python3 python3-virtualenv python3-pip boto3 botocore

python3 -m venv .venv

source .venv/bin/activate

pip install --upgrade pip setuptools wheel

pip install ansible

ansible --version
ansible [core 2.18.6]
  config file = /etc/ansible/ansible.cfg
  configured module search path = ['/home/imesquit/.ansible/plugins/modules', '/usr/share/ansible/plugins/modules']
  ansible python module location = /home/imesquit/HandsOn/AWS/RDS/ansible/.venv/lib64/python3.13/site-packages/ansible
  ansible collection location = /home/imesquit/.ansible/collections:/usr/share/ansible/collections
  executable location = /home/imesquit/HandsOn/AWS/RDS/ansible/.venv/bin/ansible
  python version = 3.13.3 (main, Apr 22 2025, 00:00:00) [GCC 14.2.1 20250110 (Red Hat 14.2.1-7)] (/home/imesquit/HandsOn/AWS/RDS/ansible/.venv/bin/python3)
  jinja version = 3.1.6
  libyaml = True

ansible-galaxy collection install amazon.aws

code ~/.aws/credentials

[default]
aws_access_key_id     = <key_id>
aws_secret_access_key = <access_key>


ansible-galaxy collection list | grep amazon.aws
amazon.aws                               9.5.0  
amazon.aws                               9.5.0


ansible-doc -l | grep aws_caller_info
amazon.aws.aws_caller_info


ansible localhost -m amazon.aws.aws_caller_info -c local
[WARNING]: Platform linux on host imesquit-thinkpadt14gen3.rmtbr.csb is using the discovered Python interpreter at /home/imesquit/HandsOn/AWS/RDS/ansible/.venv/bin/python3.13, but future installation of another Python interpreter
could change the meaning of that path. See https://docs.ansible.com/ansible-core/2.18/reference_appendices/interpreter_discovery.html for more information.
imesquit-thinkpadt14gen3.rmtbr.csb | SUCCESS => {
    "account": "363040862090",
    "account_alias": "sandbox1568-gpte",
    "ansible_facts": {
        "discovered_interpreter_python": "/home/imesquit/HandsOn/AWS/RDS/ansible/.venv/bin/python3.13"
    },
    "arn": "arn:aws:iam::363040862090:user/open-environment-8s8hl-admin",
    "changed": false,
    "user_id": "AIDAVJBXL56FJFXMQYV42"
}

ansible-vault create vault.yml


New Vault password: 
Confirm New Vault password: 

redhat

ansible-vault encrypt vault.yml

ansible-vault decrypt vault.yml

rds_password: "dbrdspass"


ansible-playbook -i localhost, aws-create-infra.yaml --ask-vault-pass


ssh -i "aws-bastion-key.pem" ec2-user@ec2-18-230-88-94.sa-east-1.compute.amazonaws.com

Uma vez dentro da instância do bastion, vamos atualizar o **BLA BLA BLA* e instalar o mysql via linha de comando. Por fim, vamos nos conectar à instância RDS onde temos nosso banco de dados. 

> *O atributo host a ser informado deve ser o definido para a instância do RDS.*
```
sudo yum update -y

sudo yum install -y mysql

mysql --host=music-api-db.cvtpccljwu07.sa-east-1.rds.amazonaws.com --port=3306 --user=admin --password musicdb
```

O resultado deve ser algo como:

*Welcome to the MariaDB monitor.  Commands end with ; or \g.*
*Your MySQL connection id is 27*
*Server version: 8.0.41 Source distribution*

*Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.*

*Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.*

*MySQL [musicdb]>*

Vamos criar o usuário dbuser que será necessário para configurar a aplicação  que vai salvar as informações no banco de dados.

```
CREATE USER 'dbuser'@'%' IDENTIFIED BY 'dbpass';

GRANT 
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    CREATE,
    ALTER,
    DROP,
    INDEX,
    REFERENCES
ON musicdb.* 
TO 'dbuser'@'%';

FLUSH PRIVILEGES;
```

curl -X POST http://localhost:8080/api/songs \  
  -H "Content-Type: application/json"    
  -d '{
        "title": "Stairway to Heaven",
        "artist": {
          "name": "Led Zeppelin"
        }
      }'

curl -X POST http://localhost:8080/api/songs \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Smoke on the Water",
        "artist": {
          "name": "Deep Purple"
        }
      }'

curl -X POST http://localhost:8080/api/songs \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Back in Black",
        "artist": {
          "name": "AC/DC"
        }
      }'

curl -X POST http://localhost:8080/api/songs \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Kashmir",
        "artist": {
          "id": 1
        }
      }'

select * from songs;

select * from artists;

select s.id, s.title, a.name from songs s inner join artists a on s.artist_id = a.id where a.name = 'Led Zeppelin';


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


## Destroy

ansible-playbook -i localhost, aws-destroy-infra.yaml --ask-vault-pass




curl -X POST https://musicplayer-app.apps.cluster-qdq57.qdq57.sandbox1229.opentlc.com/api/songs \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Smoke on the Water",
        "artist": {
          "name": "Deep Purple"
        }
      }'