ADD JAR mergeJSON.jar ;
CREATE TEMPORARY FUNCTION MergeJSON as 'edu.isi.karma.mapreduce.function.MergeJSON';
INSERT OVERWRITE TABLE ${merge_table_name}
select MergeJSON(d.json, f.json, ${json_path}) as result from ${source_table_name} f right outer join 
(select b.json as json, regexp_replace(trim(c.mbox),'\\"', '') as uri from
(select
	a.json, 
	split(regexp_replace(if(a.rawmbox IS NULL, "", a.rawmbox),'\\[|\\]','') ,',') as mbox_array
	from (select json, get_json_object(json, concat("$.", ${json_path})) as rawmbox from 
${target_table_name} ) a) b  lateral view explode(mbox_array) c as mbox) d on d.uri== get_json_object(f.json, "$.id") ;