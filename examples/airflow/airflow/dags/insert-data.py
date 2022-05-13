import datetime
import pendulum

import requests
from airflow.decorators import dag, task
from airflow.providers.postgres.hooks.postgres import PostgresHook
from airflow.providers.postgres.operators.postgres import PostgresOperator

@dag(
    schedule_interval="0 0 * * *",
    start_date=pendulum.datetime(2021, 1, 1, tz="UTC"),
    catchup=False,
    dagrun_timeout=datetime.timedelta(minutes=60),
)
def InsertDataDag():
    PostgresOperator(
        task_id="new_room_booking",
        postgres_conn_id="local-postgre",
        sql="""INSERT INTO foo VALUES ('bar')"""
    )    


dag = InsertDataDag()
