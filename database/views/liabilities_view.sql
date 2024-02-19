drop view coa_liabilities_items_view;
create view coa_liabilities_items_view as
	select id, name, headType, organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from (
		select t1.id as id, t1.name as name, 'item' as headType, t1.ORGANIZATION_ID as organization, t1.CREATED_AT, t1.CREATED_BY, t1.MODIFIED_AT, t1.MODIFIED_BY, t1.PRESENT_STATUS FROM SPECIFICS AS t1 LEFT JOIN SPECIFICS as t2 ON t1.id = t2.PARENT_SPECIFIC WHERE t1.PRESENT_STATUS=1 and t1.ACCOUNT_CODE like '4%' and (t1.IDENT_DATA_VALID is null or t1.IDENT_DATA_VALID = '') and t2.id IS NULL
		union all
		select id, name, 'vend' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from VENDOR a where PRESENT_STATUS=1 and type in (1,4)
		union all
		select id, name, 'cAdv' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from VENDOR a where PRESENT_STATUS=1 and type in (2,3)
		union all
		select id, name, '192_' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='31'
		union all
		select id, name, '194a' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='32'
		union all
		select id, name, '1941' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='33'
		union all
		select id, name, '1942' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='34'
		union all
		select id, name, '194h' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='35'
		union all
		select id, name, '1943' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='36'
		union all
		select id, name, '1944' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='37'
		union all
		select id, name, '194j' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='38'
		union all
		select id, name, '194j' as headType, ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from SPECIFICS a where PRESENT_STATUS=1 and IDENT_DATA_VALID ='38'
		union all
		select id, tax_name as name, 'taxs' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=2
		union all
		select id, tax_name as name, 'sgst' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=20
		union all
		select id, tax_name as name, 'cgst' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=21
		union all
		select id, tax_name as name, 'igst' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=22
		union all
		select id, tax_name as name, 'cess' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=23
		union all
		select id, tax_name as name, 'rmso' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=50
		union all
		select id, tax_name as name, 'rmco' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=51
		union all
		select id, tax_name as name, 'rmio' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=52
		union all
		select id, tax_name as name, 'rmeo' as headType, BRANCH_ORGANIZATION_ID as organization, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS from BRANCH_TAXES a where PRESENT_STATUS=1 and tax_type=53
	) tbl order by name;
