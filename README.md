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


ansible-playbook -i localhost, aws-create-infra.yaml --ask-vault-pass


ssh -i "aws-bastion-key.pem" ec2-user@ec2-18-230-88-94.sa-east-1.compute.amazonaws.com

sudo yum update -y

sudo yum install -y mysql

mysql --host=music-api-db.cvtpccljwu07.sa-east-1.rds.amazonaws.com --port=3306 --user=admin --password musicdb
---
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MySQL connection id is 27
Server version: 8.0.41 Source distribution

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MySQL [musicdb]>
---


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


## Destroy

ansible-playbook -i localhost, aws-destroy-infra.yaml --ask-vault-pass