# Airflow deployment to produce open lineage messages

In `json-requests` are two messages producer by airflow open lineage agent that can be used for testing.

### How to produce messages from runing airflow
1) In `airflow-image` folder build the docker image as `open-lineage-airflow`
2) Go to `airflow` folder and continue using this guide: https://airflow.apache.org/docs/apache-airflow/stable/start/docker.html#initializing-environment
3) Check `airflow/dags/insert-data.py` Before running this in airflow, prepare postgresql database with foo table mentioned in the dag.
4) In Airflow UI create new db connection (id:local-postgre)
5) In Airflow UI run the InsertDataDag

This should produce the open lineage message.
You may also need to change `OPENLINEAGE_URL` in docker-compose if your rest-proxy is on different address.
