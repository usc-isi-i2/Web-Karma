create external table ${merge_table_name} (merged_json STRING) STORED AS SEQUENCEFILE LOCATION ${output_directory} ;
