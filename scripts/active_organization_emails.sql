-- Get the organization ids that have the max number of devices reporting
-- (meaning that they probably released a version of their app with FlowUp)
-- With that, we get every user and their emails of the organizations that
-- were previously selected.
SELECT u.email, u.name
FROM user u, organization_user ou
WHERE ou.user_id = u.id
  AND u.email NOT LIKE '%karumi.com'
  AND ou.organization_id IN (
    SELECT o.id
    FROM allowed_uuid au, organization o, application a
    WHERE au.api_key_id = o.api_key_id
      AND o.id = a.organization_id
    GROUP BY au.api_key_id
    HAVING COUNT(au.api_key_id) >= 20 -- Update 20 by the max number of allowed UUIDs
  );