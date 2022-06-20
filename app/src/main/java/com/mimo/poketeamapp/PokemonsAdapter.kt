package com.mimo.poketeamapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.mimo.poketeamapp.database.AppDatabase
import com.mimo.poketeamapp.databinding.PokemonItemBinding
import com.mimo.poketeamapp.model.Pokemon
import com.squareup.picasso.Picasso

class PokemonsAdapter(private val pokemons: List<Pokemon>): RecyclerView.Adapter<PokemonsAdapter.PokemonViewHolder>() {

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val item = pokemons[position]
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PokemonViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.pokemon_item, parent, false), parent
    )

    override fun getItemCount() = pokemons.size

    class PokemonViewHolder(view: View, private val parent: ViewGroup): RecyclerView.ViewHolder(view) {
        private val binding = PokemonItemBinding.bind(view)
        val db = Room
            .databaseBuilder(parent.context, AppDatabase::class.java, "pokemon-database")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        fun bind(pokemon: Pokemon) {
            if(parent.context is PokeTeamActivity) {
                binding.add.visibility = View.GONE
            } else {
                binding.remove.visibility = View.GONE
            }
            binding.pokemonName.text = pokemon.name
            binding.pokemonBaseExperience.text = pokemon.base_experience.toString()
            if(pokemon.sprites == null && pokemon.image != null && pokemon.image.isNotEmpty()) {
                Picasso.get().load(pokemon.image).into(binding.pokemonImage)
            } else {
                Picasso.get().load(pokemon.sprites?.other?.home?.front_default).into(binding.pokemonImage)
            }

            binding.remove.setOnClickListener {
                db.pokemonDao().removeFavorite(pokemon.id)
                // TODO lograr notifydatasetchanged
            }

            binding.add.setOnClickListener {
                val count = db.pokemonDao().getFavoritesCount()
                if(count < maxPokemonsToBeFavorites ) {
                    if(db.pokemonDao().isPokemonFavorite(pokemon.id)) {
                        Toast.makeText(parent.context, R.string.already_favorite, Toast.LENGTH_SHORT).show()
                    } else {
                        if (pokemon.url != null) {
                            db.pokemonDao().addFavorite(
                                pokemon.name,
                                pokemon.url,
                                pokemon.id,
                                pokemon.sprites?.other?.home?.front_default!!,
                                pokemon.base_experience)
                        } else {
                            db.pokemonDao().addFavorite(
                                pokemon.name,
                                "",
                                pokemon.id,
                                pokemon.sprites?.other?.home?.front_default!!,
                                pokemon.base_experience)
                        }
                        Toast.makeText(parent.context, R.string.added_favorite, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(parent.context, R.string.favorite_max_reached, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}