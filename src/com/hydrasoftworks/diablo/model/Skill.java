package com.hydrasoftworks.diablo.model;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.annotations.SerializedName;

public class Skill
{
	private static final String	SKILL_URL		= "d3/en/tooltip/";
	private static final String	IMAGE_LINK		= "http://eu.media.blizzard.com/d3/icons/skills/64/";
	public static final String	ACTIVE_SKILL	= "active";
	public static final String	PASSIVE_SKILL	= "passive";
	
	@SerializedName("skill")
	private SkillInfo			skillInfo;
	private Rune				rune;
	
	public String createTooltipLink()
	{
		return CareerProfile.getActiveProfile().getHost() + SKILL_URL + getSkillInfo().tooltipUrl + (getRune() != null ? "?runeType=" + getRune().getType() : "");
	}
	
	public static class SkillInfo
	{
		private static final String	IMAGE_LINK	= "http://eu.media.blizzard.com/d3/icons/skills/42/";
		private String				slug;
		private String				name;
		private String				icon;
		private String				tooltipUrl;
		private String				description;
		private String				simpleDescription;
		
		public URL createImageLink() throws MalformedURLException
		{
			return new URL(IMAGE_LINK + icon + ".png");
		}
		
		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}
	}
	
	public static class Rune
	{
		private String	slug;
		private String	type;
		private String	name;
		private String	description;
		@SerializedName("simpleDescription")
		private String	simpleDescription;
		@SerializedName("tooltipParams")
		private String	tooltipParams;
		@SerializedName("orderIndex")
		private int		orderIndex;
		
		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * @return the type
		 */
		public String getType()
		{
			return type;
		}
	}
	
	/**
	 * @return the skillInfo
	 */
	public SkillInfo getSkillInfo()
	{
		return skillInfo;
	}
	
	/**
	 * @return the rune
	 */
	public Rune getRune()
	{
		return rune;
	}
}
